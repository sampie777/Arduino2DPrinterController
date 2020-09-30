package hardware

import com.fazecast.jSerialComm.SerialPort

interface PrintingDevice {
    var state: PrinterState

    fun getComPort(): SerialPort?
    fun connect(deviceName: String, baudRate: Int): Boolean
    fun disconnect() {}
    fun processSerialInput(data: List<String>)
}