object App {
    @Volatile
    var isPaused = false

    @Suppress("ControlFlowWithEmptyBody")
    fun waitForPauseBlocking() {
        while (isPaused) {}
    }
}