package hardware.serial


import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import hardware.HardwareDevice
import java.util.logging.Logger

class SerialListener(private val hardwareDevice: HardwareDevice) : SerialPortDataListener {
    private val logger = Logger.getLogger(SerialListener::class.java.name)

    var currentDataLine: String = ""
    val receivedDataLines = ArrayList<String>()

    override fun getListeningEvents(): Int {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED
    }

    override fun serialEvent(event: SerialPortEvent) {
        if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
            logger.warning("Got invalid event type: ${event.eventType}")
            return
        }

        currentDataLine += String(event.receivedData)
        val messages = currentDataLine
            .split("\n")
            .map { it.trim('\r') }

        val terminatedMessages = messages.subList(0, messages.size - 1)
        currentDataLine = messages.last()

        receivedDataLines.addAll(terminatedMessages)
        terminatedMessages.forEach {
            println(it)
        }

        hardwareDevice.processSerialInput(terminatedMessages)
    }

    fun send(data: String) {
        logger.fine("Sending data to serial device: $data")
        if (hardwareDevice.getComPort() == null) {
            logger.warning("Serial device unconnected, cannot send data")
            return
        }

        val dataBytes = data.toByteArray()
        val writtenBytes = hardwareDevice.getComPort()?.writeBytes(dataBytes, dataBytes.size.toLong())

        if (writtenBytes != dataBytes.size) {
            logger.warning("Not all bytes were sent. Only $writtenBytes out of ${dataBytes.size}")
        }
    }

    fun clear() {
        logger.fine("Clearing serial data buffer")
        receivedDataLines.clear()
        currentDataLine = ""
    }
}