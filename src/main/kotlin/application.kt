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

    while (connection) {
        Thread.sleep(100)

        if (App.isPaused) continue

        if (Printer.state != PrinterState.IDLE
            && Printer.state != PrinterState.PRINTING) continue

        if (App.isDrawingFinished) continue

        Printer.resetHead()
        drawLines()
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
    App.isDrawingFinished = false

    val zPosition = 21.0

    Printer.blueprint = handDrawingPoints

    // Set head to nearest location by going along the edge of the paper
    Printer.lineTo(handDrawingPoints[0][1], handDrawingPoints[0][0], 0.0)
    Printer.lineTo(handDrawingPoints[0][1], handDrawingPoints[0][0], zPosition) // Lower head

    handDrawingPoints.forEach {
        Printer.lineTo(it[1], it[0], zPosition)
    }
    Printer.lineTo(
        handDrawingPoints[0][1],
        handDrawingPoints[0][0],
        zPosition
    )    // Close gap between last and first point
    Printer.lineTo(handDrawingPoints[0][1], handDrawingPoints[0][0], 0.0)    // Lift head


    // Move head to reset position
    Printer.lineTo(0.0, 0.0, 0.0)

    logger.info("Drawing is done")
    App.isDrawingFinished = true
}