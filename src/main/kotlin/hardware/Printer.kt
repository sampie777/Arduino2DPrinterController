package hardware

import com.fazecast.jSerialComm.SerialPort
import hardware.serial.SerialListener
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

object Printer : HardwareDevice {
    private val logger = Logger.getLogger(Printer::class.java.name)

    @Volatile
    private var comPort: SerialPort? = null
    override fun getComPort() = comPort

    @Volatile
    private var _isReady: Boolean = false
    override fun isReady(): Boolean = _isReady

    private val motorX = Motor("X", 4714 / 50.0, "2: target reached")
    private val motorY = Motor("Y", 4351 / 30.0, "7: target reached")

    override fun setReady(value: Boolean) {
        _isReady = value

        if (!_isReady) {
            logger.info("Printer is not ready")
            return
        }
        logger.info("Printer is ready")

        logger.info("Turning off sweep")
        // Turn off sweep
        serialListener.send("s")
    }

    private val serialListener = SerialListener(this)

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        comPort = SerialPort.getCommPorts().find { it.systemPortName == deviceName }
        if (comPort == null) {
            logger.severe("Serial device '$deviceName' not found")
            return false
        }

        comPort!!.baudRate = baudRate
        val connected = comPort!!.openPort()

        if (!connected) {
            logger.severe("Could not connect to hardware device '$deviceName'")
            return false
        }

        logger.info("Connected to hardware device '$deviceName'")
        clearComPort()
        comPort!!.addDataListener(serialListener)

        return true
    }

    override fun disconnect() {
        logger.info("Disconnecting hardware")
        comPort?.closePort()
        logger.info("Hardware device disconnected")
    }

    override fun processSerialInput(data: List<String>) {
        if ("Boot done." in data) {
            setReady(true)
        }

        if (motorX.targetReachedIdentifier in data) {
            logger.info("Motor ${motorX.name} reached target")
            motorX.targetReached = true
        }
        if (motorY.targetReachedIdentifier in data) {
            logger.info("Motor ${motorY.name} reached target")
            motorY.targetReached = true
        }
    }

    private fun clearComPort() {
        logger.info("Clearing com port buffer...")
        while (comPort!!.bytesAvailable() > 0) {
            val byteBuffer = ByteArray(comPort!!.bytesAvailable())
            comPort?.readBytes(byteBuffer, byteBuffer.size.toLong())
        }
        logger.info("Com port buffer cleared")
    }

    fun moveTo(x: Double, y: Double, waitForMotors: Boolean = false) {
        logger.info("Moving to $x, $y")

        motorX.targetReached = motorX.position == x
        motorX.position = x
        motorY.targetReached = motorY.position == x
        motorY.position = y

        val paddedX = (x * 10).roundToInt().toString().padStart(4, '0')
        val paddedY = (y * 10).roundToInt().toString().padStart(4, '0')
        serialListener.send("x${paddedX}y${paddedY}")

        if (!waitForMotors) return

        waitForMotors()
    }

    fun waitForMotors() {
        logger.info("Waiting for motors to reach position")
        while (!(motorX.targetReached && motorY.targetReached)) {}
        logger.info("Motor positions reached")
    }
}