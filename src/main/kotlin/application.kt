import com.fazecast.jSerialComm.SerialPort
import config.Config
import gui.MainFrame
import hardware.Printer
import hardware.PrinterState
import java.awt.EventQueue
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("Application")

fun main(args: Array<String>) {
    if (args.contains("--list-devices")) {
        listSerialPorts()
        return
    }

    if (args.contains("--virtual")) {
        logger.info("Running as virtual device")
        Config.runVirtual = true
    }

    attachExitCatcher()

    EventQueue.invokeLater {
        MainFrame.createAndShow()
    }

    val connection = Printer.connect(Config.deviceName, Config.baudRate)
    if (!connection) {
        exitApplication()
    }

    var drawingDone = false
    while (connection) {
        if (Printer.state != PrinterState.IDLE && Printer.state != PrinterState.PRINTING) continue

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


    // Set head to nearest location for going along the edge of the paper
    if (handDrawingPoints[0][0] > handDrawingPoints[0][1]) {
        Printer.lineTo(0.0, handDrawingPoints[0][0])
    } else {
        Printer.lineTo(handDrawingPoints[0][1], 0.0)
    }
    Printer.lineTo(0.0, 0.0)

    logger.info("Drawing is done")
}