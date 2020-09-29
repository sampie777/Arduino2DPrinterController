package events

import java.util.logging.Logger

object EventsHub : SerialEventListener, PrinterEventListener{
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

    override fun dataReceived(data: List<String>) {
        logger.finer("Sending dataReceived event")
        val copyList = serialEventListeners.toTypedArray()
        copyList.forEach {
            it.dataReceived(data)
        }
    }

    override fun dataSend(data: String) {
        logger.finer("Sending dataSend event")
        val copyList = serialEventListeners.toTypedArray()
        copyList.forEach {
            it.dataSend(data)
        }
    }

    override fun newPosition(x: Double, y: Double) {
        logger.finer("Sending newPosition event")
        val copyList = printerEventListeners.toTypedArray()
        copyList.forEach {
            it.newPosition(x, y)
        }
    }

    override fun targetReached(x: Double, y: Double) {
        logger.finer("Sending targetReached event")
        val copyList = printerEventListeners.toTypedArray()
        copyList.forEach {
            it.targetReached(x, y)
        }
    }
}