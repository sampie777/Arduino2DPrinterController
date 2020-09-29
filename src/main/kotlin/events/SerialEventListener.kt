package events

interface SerialEventListener {
    fun dataReceived(data: List<String>) {}
    fun dataSend(data: String) {}
}
