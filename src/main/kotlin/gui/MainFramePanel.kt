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

        val bottomPanel = JPanel()
        bottomPanel.layout = BorderLayout(10, 10)
        bottomPanel.add(SerialDataPanel(), BorderLayout.CENTER)
        bottomPanel.add(ControlPanel(), BorderLayout.LINE_END)

        add(MotorPositionTracker(), BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.PAGE_END)
    }
}