package gui

import java.awt.BorderLayout
import java.util.logging.Logger
import javax.swing.JPanel
import javax.swing.border.EmptyBorder


class MainFramePanel : JPanel() {
    private val logger = Logger.getLogger(MainFramePanel::class.java.name)

    init {
        createGui()
    }

    private fun createGui() {
        border = EmptyBorder(10, 10, 10, 10)
        layout = BorderLayout(10, 10)

        add(MotorPositionTracker(), BorderLayout.CENTER)
        add(SerialDataPanel(), BorderLayout.PAGE_END)
    }
}