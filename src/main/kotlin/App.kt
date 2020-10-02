import events.EventsHub

object App {
    @Volatile
    var isPaused = false

    @Volatile
    var isDrawingFinished = false
        set(value) {
            field = value
            EventsHub.appPropertyChanged("isDrawingFinished", value)
        }

    @Suppress("ControlFlowWithEmptyBody")
    fun waitForPauseBlocking() {
        while (isPaused) {
        }
    }
}