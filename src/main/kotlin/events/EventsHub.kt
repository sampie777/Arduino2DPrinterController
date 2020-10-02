package events

import hardware.PrinterState
import java.util.logging.Logger

object EventsHub : SerialEventListener, PrinterEventListener, AppEventListener {
    private val logger = Logger.getLogger(EventsHub::class.java.name)

    private val serialEventListeners = hashSetOf<SerialEventListener>()
    private val printerEventListeners = hashSetOf<PrinterEventListener>()
    private val appEventListeners = hashSetOf<AppEventListener>()

    fun register(listener: SerialEventListener) {
        logger.info("Adding SerialEventListener")
        serialEventListeners.add(listener)
    }

    fun register(listener: PrinterEventListener) {
        logger.info("Adding PrinterEventListener")
        printerEventListeners.add(listener)
    }

    fun register(listener: AppEventListener) {
        logger.info("Adding AppEventListener")
        appEventListeners.add(listener)
    }

    /*
    Serial events
     */

    override fun dataReceived(data: List<String>) {
        logger.finer("Sending dataReceived event")
        serialEventListeners.toTypedArray().forEach {
            Thread { it.dataReceived(data) }.start()
        }
    }

    override fun dataSend(data: String) {
        logger.finer("Sending dataSend event")
        serialEventListeners.toTypedArray().forEach {
            Thread { it.dataSend(data) }.start()
        }
    }

    /*
    Printer events
     */

    override fun newPosition(x: Double, y: Double, z: Double) {
        logger.finer("Sending newPosition event")
        printerEventListeners.toTypedArray().forEach {
            Thread { it.newPosition(x, y, z) }.start()
        }
    }

    override fun targetReached(x: Double, y: Double, z: Double) {
        logger.finer("Sending targetReached event")
        printerEventListeners.toTypedArray().forEach {
            Thread { it.targetReached(x, y, z) }.start()
        }
    }

    override fun stateChanged(newState: PrinterState) {
        logger.finer("Sending stateChanged event")
        printerEventListeners.toTypedArray().forEach {
            Thread { it.stateChanged(newState) }.start()
        }
    }

    /*
    App events
     */
    override fun appPropertyChanged(propertyName: String, newValue: Any?) {
        logger.finer("Sending appPropertyChanged event")
        appEventListeners.toTypedArray().forEach {
            Thread { it.appPropertyChanged(propertyName, newValue) }.start()
        }
    }
}