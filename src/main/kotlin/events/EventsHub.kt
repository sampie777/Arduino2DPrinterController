package events

import hardware.PrinterState
import java.util.logging.Logger

object EventsHub : SerialEventListener, PrinterEventListener {
    private val logger = Logger.getLogger(EventsHub::class.java.name)

    private val serialEventListeners = hashSetOf<SerialEventListener>()
    private val printerEventListeners = hashSetOf<PrinterEventListener>()

    fun register(listener: SerialEventListener) {
        logger.info("Adding SerialEventListener")
        serialEventListeners.add(listener)
    }

    fun register(listener: PrinterEventListener) {
        logger.info("Adding PrinterEventListener")
        printerEventListeners.add(listener)
    }

    /*
    Serial events
     */

    override fun dataReceived(data: List<String>) {
        logger.finer("Sending dataReceived event")
        serialEventListeners.toTypedArray().forEach {
            it.dataReceived(data)
        }
    }

    override fun dataSend(data: String) {
        logger.finer("Sending dataSend event")
        serialEventListeners.toTypedArray().forEach {
            it.dataSend(data)
        }
    }

    /*
    Printer events
     */

    override fun newPosition(x: Double, y: Double) {
        logger.finer("Sending newPosition event")
        printerEventListeners.toTypedArray().forEach {
            it.newPosition(x, y)
        }
    }

    override fun targetReached(x: Double, y: Double) {
        logger.finer("Sending targetReached event")
        printerEventListeners.toTypedArray().forEach {
            it.targetReached(x, y)
        }
    }

    override fun stateChanged(newState: PrinterState) {
        logger.finer("Sending stateChanged event")
        printerEventListeners.toTypedArray().forEach {
            it.stateChanged(newState)
        }
    }
}