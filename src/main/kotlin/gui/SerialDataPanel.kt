package gui


import events.EventsHub
import events.SerialEventListener
import java.awt.BorderLayout
import java.awt.TextArea
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.swing.JPanel


class SerialDataPanel : JPanel(), SerialEventListener {
    private val logger = Logger.getLogger(SerialDataPanel::class.java.name)

    private val textField = TextArea()

    init {
        initGui()

        EventsHub.register(this)
    }

    private fun initGui() {
        layout = BorderLayout()

        textField.isEditable = false
        add(textField, BorderLayout.CENTER)
    }

    override fun dataReceived(data: List<String>) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        data.forEach {
            textField.append("<[$timestamp] ${it}\n")
        }
    }

    override fun dataSend(data: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        textField.append(">[$timestamp] ${data}\n")
    }
}