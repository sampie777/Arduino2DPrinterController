package hardware

enum class PrinterState {
    NOT_CONNECTED,
    BOOTING,
    SWEEPING,
    CALIBRATING,
    RESETTING,
    PRINTING,
    IDLE
}