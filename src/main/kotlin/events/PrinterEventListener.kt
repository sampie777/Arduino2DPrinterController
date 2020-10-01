package events

import hardware.PrinterState

interface PrinterEventListener {
    fun newPosition(x: Double, y: Double, z: Double) {}
    fun targetReached(x: Double, y: Double, z: Double) {}
    fun stateChanged(newState: PrinterState) {}
}
