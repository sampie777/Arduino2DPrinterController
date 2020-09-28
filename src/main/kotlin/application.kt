import com.fazecast.jSerialComm.SerialPort
import gui.MainFrame
import hardware.Printer
import java.awt.EventQueue
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

    EventQueue.invokeLater {
        MainFrame.createAndShow()
    }

    val connection = Printer.connect(deviceName, baudRate)

    var drawingDone = false
    while (connection) {
        if (!Printer.isReady) continue

        if (drawingDone) continue

        drawLines()
        drawingDone = true
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

fun drawLines() {
    logger.info("Start drawing")

    // Set head to nearest location by going along the edge of the paper
    if (handDrawingPoints[0][0] > handDrawingPoints[0][1]) {
        Printer.lineTo(0.0, handDrawingPoints[0][0])
    } else {
        Printer.lineTo(handDrawingPoints[0][1], 0.0)
    }

    handDrawingPoints.forEach {
        Printer.lineTo(it[1], it[0])
    }
    Printer.lineTo(handDrawingPoints[0][1], handDrawingPoints[0][0])    // Close gap between last and first point

    logger.info("Drawing is done")
}