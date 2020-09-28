import com.fazecast.jSerialComm.SerialPort
import hardware.Printer
import java.util.logging.Logger

val logger: Logger = Logger.getLogger("Application")

fun main(args: Array<String>) {
    val deviceName = "ttyUSB0"
    val baudRate = 115200

    if (args.contains("--list-devices")) {
        listSerialPorts()
        return
    }

    attachExitCatcher()

    val connection = Printer.connect(deviceName, baudRate)

    @Suppress("ControlFlowWithEmptyBody")
    while (connection) {
        if (!Printer.isReady()) {
            continue
        }

        Thread.sleep(1000)
        Printer.moveTo(0.0, 0.0, waitForMotors = true)
        Thread.sleep(1000)

        (0..100).forEach {
            Printer.moveTo(it / 10.0 * 3, it / 10.0 * 3, waitForMotors = true)
        }
    }

    logger.info("Connection lost")

    Printer.disconnect()
}

fun listSerialPorts() {
    SerialPort.getCommPorts().forEach {
        println("- ${it.descriptivePortName} \t[${it.systemPortName}]")
        logger.info("- ${it.descriptivePortName} \t[${it.systemPortName}]")
    }
}

fun attachExitCatcher() {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            exitApplication()
        }
    })
}

fun exitApplication() {
    logger.info("Exiting application...")

    Printer.disconnect()

    logger.info("Shutdown finished")
}