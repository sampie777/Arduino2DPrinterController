package hardware

import com.fazecast.jSerialComm.SerialPort

interface HardwareDevice {
    fun getComPort(): SerialPort?
    fun isReady(): Boolean
    fun setReady(value: Boolean)
    fun connect(deviceName: String, baudRate: Int): Boolean
    fun disconnect() {}
    fun processSerialInput(data: List<String>)
}