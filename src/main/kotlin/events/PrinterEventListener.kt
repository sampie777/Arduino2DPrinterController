package events

interface PrinterEventListener {
    fun newPosition(x: Double, y: Double) {}
    fun targetReached(x: Double, y: Double) {}
}
