package gui

import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.*
import java.util.logging.Logger
import javax.swing.JPanel
import javax.swing.border.EmptyBorder


class MainFramePanel : JPanel() {
    private val logger = Logger.getLogger(MainFramePanel::class.java.name)

    private val screenUpdateTimer = Timer()
    private var isRepainting = false

    init {
        createGui()

        startScreenUpdateTimer()
    }

    private fun createGui() {
        border = EmptyBorder(10, 10, 10, 10)
        layout = BorderLayout(10, 10)

        add(MotorPositionTracker(), BorderLayout.CENTER)
    }

    override fun paintComponent(g: Graphics) {
        isRepainting = true
        super.paintComponents(g as Graphics2D)
        isRepainting = false
    }

    fun isRepainting(): Boolean {
        return isRepainting
    }

    fun startScreenUpdateTimer() {
        screenUpdateTimer.schedule(object : TimerTask() {
            override fun run() {
                if (isRepainting()) {
                    logger.fine("Skipping paint update: still updating")
                    return
                }

                repaint()
            }
        }, 0, 1000 / 25)
    }

    fun stopScreenUpdateTimer() {
        screenUpdateTimer.cancel()
    }
}