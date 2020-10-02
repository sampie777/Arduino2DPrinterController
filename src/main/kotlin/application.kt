import com.fazecast.jSerialComm.SerialPort
import config.Config
import drawings.rectangleDrawingPoints
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

    while (connection) {
        Thread.sleep(100)

        if (App.isPaused) continue

        if (Printer.state != PrinterState.IDLE
            && Printer.state != PrinterState.PRINTING) continue

        if (App.isDrawingFinished) continue

        Printer.resetHead()
        drawLines(rectangleDrawingPoints)
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

fun drawLines(drawingPoints: Array<Array<Double>>) {
    logger.info("Start drawing")
    App.isDrawingFinished = false

    Printer.blueprint = drawingPoints
    val zPosition = Config.headDownPosition

    // Set head to nearest location by going along the edge of the paper
    Printer.lineTo(drawingPoints[0][1], drawingPoints[0][0], 0.0)
    Printer.lineTo(drawingPoints[0][1], drawingPoints[0][0], zPosition) // Lower head

    drawingPoints.forEach {
        Printer.lineTo(it[1], it[0], zPosition)
    }

    Printer.lineTo(drawingPoints[0][1], drawingPoints[0][0], 0.0)    // Lift head

    logger.info("Drawing is done")
    App.isDrawingFinished = true
    Printer.resetHead(waitForMotors = false)
}