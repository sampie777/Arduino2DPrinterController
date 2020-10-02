package gui


import App
import events.AppEventListener
import events.EventsHub
import hardware.Printer
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.logging.Logger
import javax.swing.*

class ControlPanel : JPanel(), AppEventListener {
    private val logger = Logger.getLogger(ControlPanel::class.java.name)

    private val commandTextField = JTextField()
    private val restartButton = JButton("Restart")

    init {
        initGui()

        EventsHub.register(this)
    }

    private fun initGui() {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

        val pauseButton = JButton(if (App.isPaused) "Resume" else "Pause")
        pauseButton.addActionListener {
            logger.info("Pause button clicked")
            App.isPaused = !App.isPaused

            pauseButton.text = if (App.isPaused) "Resume" else "Pause"
        }

        restartButton.isEnabled = App.isDrawingFinished
        restartButton.addActionListener {
            logger.info("restartButton clicked")
            App.isDrawingFinished = false
        }

        val headUpButton = JButton("Head up")
        headUpButton.addActionListener {
            logger.info("Head-up button clicked")
            Printer.moveTo(
                Printer.motorX.position, Printer.motorY.position, 0.0,
                waitForMotors = false,
                ignorePause = true
            )
        }

        val sendCommandButton = JButton(">")
        sendCommandButton.addActionListener {
            logger.info("sendCommandButton button clicked")
            sendCommandText()
        }

        commandTextField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
            }

            override fun keyPressed(e: KeyEvent) {
            }

            override fun keyReleased(e: KeyEvent) {
                if (e.keyChar == '\n') {
                    sendCommandText()
                }
            }
        })

        val commandFieldPanel = JPanel()
        commandFieldPanel.layout = BorderLayout(3, 3)
        commandFieldPanel.preferredSize = Dimension(200, 25)
        commandFieldPanel.maximumSize = Dimension(300, 0)
        commandFieldPanel.add(commandTextField, BorderLayout.CENTER)
        commandFieldPanel.add(sendCommandButton, BorderLayout.LINE_END)

        add(pauseButton)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(restartButton)
        add(Box.createRigidArea(Dimension(0, 10)))
        add(headUpButton)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(commandFieldPanel)
        add(Box.createVerticalGlue())
    }

    private fun sendCommandText() {
        Printer.serialListener.send(commandTextField.text)
        commandTextField.text = ""
    }

    override fun appPropertyChanged(propertyName: String, newValue: Any?) {
        restartButton.isEnabled = App.isDrawingFinished
    }
}