package hardware

import com.fazecast.jSerialComm.SerialPort
import config.Config
import events.EventsHub
import hardware.serial.SerialListener
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object Printer : PrintingDevice {
    private val logger = Logger.getLogger(Printer::class.java.name)

    @Volatile
    private var comPort: SerialPort? = null
    override fun getComPort() = comPort

    @Volatile
    override var state: PrinterState = PrinterState.NOT_CONNECTED
        set(value) {
            if (value != field) {
                logger.info("New printer state: ${value.name}")
            }

            field = value

            EventsHub.stateChanged(value)
        }

    val motorX = Motor("X", Config.serialStringMotorXTargetReached, 0.08)
    val motorY = Motor("Y", Config.serialStringMotorYTargetReached, 0.23)

    private val serialListener = SerialListener(this)

    override fun connect(deviceName: String, baudRate: Int): Boolean {
        if (Config.runVirtual) {
            logger.info("Connecting to virtual printer")
            Thread.sleep(500)
            serialListener.onDataReceived("${Config.serialStringIsBooting}\n".toByteArray())
            Thread.sleep(1000)
            serialListener.onDataReceived("${Config.serialStringCalibratingMotorX}\n".toByteArray())
            serialListener.onDataReceived("${Config.serialStringCalibratingMotorY}\n".toByteArray())
            Thread.sleep(2000)
            serialListener.onDataReceived("${Config.serialStringBootDone}\n".toByteArray())
            return true
        }
        logger.info("Connecting to serial device '$deviceName' with baud rate $baudRate")

        comPort = SerialPort.getCommPorts().find { it.systemPortName == deviceName }
        if (comPort == null) {
            logger.severe("Serial device '$deviceName' not found")
            state = PrinterState.NOT_CONNECTED
            return false
        }

        comPort!!.baudRate = baudRate
        val connected = comPort!!.openPort()

        if (!connected) {
            logger.severe("Could not connect to hardware device '$deviceName'")
            state = PrinterState.NOT_CONNECTED
            return false
        }

        logger.info("Connected to hardware device '$deviceName'")
        clearComPort()
        comPort!!.addDataListener(serialListener)

        state = PrinterState.BOOTING
        return true
    }

    override fun disconnect() {
        logger.info("Disconnecting hardware")
        if (!Config.runVirtual) {
            comPort?.closePort()
        }
        state = PrinterState.NOT_CONNECTED
        logger.info("Hardware device disconnected")
    }

    private fun clearComPort() {
        logger.fine("Clearing com port buffer...")
        while (comPort!!.bytesAvailable() > 0) {
            val byteBuffer = ByteArray(comPort!!.bytesAvailable())
            comPort?.readBytes(byteBuffer, byteBuffer.size.toLong())
        }
        logger.fine("Com port buffer cleared")
    }

    override fun processSerialInput(data: List<String>) {
        if (Config.serialStringIsBooting in data) {
            logger.info("Printer is booting")
            state = PrinterState.BOOTING
        }
        if (Config.serialStringCalibratingMotorX in data || Config.serialStringCalibratingMotorY in data) {
            logger.info("Printer is calibrating")
            state = PrinterState.CALIBRATING
        }

        if (Config.serialStringBootDone in data) {
            logger.info("Printer is done booting")
            state = PrinterState.SWEEPING
            setSweep(false)
        }

        if (Config.serialStringSweepOn in data
            || Config.serialStringSweepUpX in data || Config.serialStringSweepDownX in data
            || Config.serialStringSweepUpY in data || Config.serialStringSweepDownY in data
        ) {
            state = PrinterState.SWEEPING
            setSweep(false)
        }

        if (Config.serialStringSweepOff in data) {
            logger.info("Sweeping disabled, printer is ready")
            state = PrinterState.IDLE
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

    fun moveTo(x: Double, y: Double, waitForMotors: Boolean = false) {
        logger.fine("Moving to $x, $y")

        motorX.setTargetPosition(x)
        motorY.setTargetPosition(y)

        val paddedX = (x * 10).roundToInt().toString().padStart(4, '0')
        val paddedY = (y * 10).roundToInt().toString().padStart(4, '0')
        serialListener.send("x${paddedX}y${paddedY}")

        EventsHub.newPosition(motorX.position, motorY.position)

        if (!waitForMotors) return

        waitForMotors()
    }

    fun waitForMotors() {
        logger.fine("Waiting for motors to reach position")

        if (Config.runVirtual) {
            serialListener.onDataReceived("${Config.serialStringMotorXTargetReached}\n".toByteArray())
            serialListener.onDataReceived("${Config.serialStringMotorYTargetReached}\n".toByteArray())
            Thread.sleep(Config.runVirtualSpeed)
        }

        @Suppress("ControlFlowWithEmptyBody")
        while (!(motorX.targetReached && motorY.targetReached)) {
        }

        EventsHub.targetReached(motorX.target, motorY.target)
        logger.fine("Motor positions reached")
    }

    fun lineTo(x: Double, y: Double) {
        logger.fine("Line to $x, $y")
        state = PrinterState.PRINTING

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
        state = PrinterState.IDLE
    }

    fun resetHead() {
        serialListener.send(Config.serialStringCalibrateMotors)

        if (Config.runVirtual) {
            Thread.sleep(10)
            serialListener.onDataReceived("${Config.serialStringCalibratingMotorX}\n".toByteArray())
            serialListener.onDataReceived("${Config.serialStringCalibratingMotorY}\n".toByteArray())
        }

        waitForMotors()
    }

    fun setSweep(on: Boolean) {
        serialListener.send(if (on) Config.serialStringTurnSweepOn else Config.serialStringTurnSweepOff)

        if (Config.runVirtual) {
            Thread.sleep(10)
            serialListener.onDataReceived("${if (on) Config.serialStringSweepOn else Config.serialStringSweepOff}\n".toByteArray())
        }
    }
}