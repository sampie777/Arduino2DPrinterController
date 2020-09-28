package hardware

import com.fazecast.jSerialComm.SerialPort
import hardware.serial.SerialListener
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object Printer : HardwareDevice {
    private val logger = Logger.getLogger(Printer::class.java.name)

    @Volatile
    private var comPort: SerialPort? = null
    override fun getComPort() = comPort

    @Volatile
    override var isReady: Boolean = false

    val motorX = Motor("X", "2: target reached", 0.08)
    val motorY = Motor("Y", "7: target reached", 0.23)

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
            logger.info("Printer is ready")
            isReady = true
            sweep(false)
        }

        if (motorX.targetReachedIdentifier in data) {
            logger.fine("Motor ${motorX.name} reached target")
            motorX.targetReached = true
            motorX.position = motorX.target
        }
        if (motorY.targetReachedIdentifier in data) {
            logger.fine("Motor ${motorY.name} reached target")
            motorY.targetReached = true
            motorY.position = motorY.target
        }
    }

    private fun clearComPort() {
        logger.fine("Clearing com port buffer...")
        while (comPort!!.bytesAvailable() > 0) {
            val byteBuffer = ByteArray(comPort!!.bytesAvailable())
            comPort?.readBytes(byteBuffer, byteBuffer.size.toLong())
        }
        logger.fine("Com port buffer cleared")
    }

    fun moveTo(x: Double, y: Double, waitForMotors: Boolean = false) {
        logger.fine("Moving to $x, $y")

        motorX.setTargetPosition(x)
        motorY.setTargetPosition(y)

        val paddedX = (x * 10).roundToInt().toString().padStart(4, '0')
        val paddedY = (y * 10).roundToInt().toString().padStart(4, '0')
        serialListener.send("x${paddedX}y${paddedY}")

        if (!waitForMotors) return

        waitForMotors()
    }

    fun waitForMotors() {
        logger.fine("Waiting for motors to reach position")
        while (!(motorX.targetReached && motorY.targetReached)) {
        }
        logger.fine("Motor positions reached")
    }

    fun lineTo(x: Double, y: Double) {
        logger.info("Line to $x, $y")

        val xDiff = x - motorX.position
        val yDiff = y - motorY.position

        val stepsX = abs(xDiff / motorX.minimumStepDistance).roundToInt()
        val stepsY = abs(yDiff / motorY.minimumStepDistance).roundToInt()

        val steps = max(stepsX, stepsY)

        val startX = motorX.position
        val startY = motorY.position
        (1..steps).forEach { step ->
            moveTo(
                motorX.roundToMinimumDistance(startX + xDiff * step.toDouble() / steps),
                motorY.roundToMinimumDistance(startY + yDiff * step.toDouble() / steps),
                waitForMotors = true
            )
        }
    }

    fun resetHead() {
        serialListener.send("f")
        waitForMotors()
    }

    fun sweep(on: Boolean) {
        serialListener.send("s" + (if (on) "1" else "0"))
    }
}