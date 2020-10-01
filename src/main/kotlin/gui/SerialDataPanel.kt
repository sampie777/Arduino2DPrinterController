package gui


import config.Config
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
        layout = BorderLayout(10, 10)

        textField.isEditable = false
        add(textField, BorderLayout.CENTER)
    }

    override fun dataReceived(data: List<String>) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        data.forEach {
            // Ignore target reached
            if (it == Config.serialStringMotorXTargetReached
                || it == Config.serialStringMotorYTargetReached
                || it == Config.serialStringMotorZTargetReached) {
                return@forEach
            }

            if (it == "[Serial] New target steps:"
                || it.matches("\\tX = \\d+".toRegex())
                || it.matches("\\tY = \\d+".toRegex())
                || it.matches("\\tZ = \\d+".toRegex())
            ) {
                return@forEach
            }

            textField.append("<[$timestamp] ${it}\n")
        }
    }

    override fun dataSend(data: String) {
        println("Sending: $data")
        // Ignore coordinates
        if (data.matches("^x\\d{4}y\\d{4}z\\d{4}\n?$".toRegex())) {
//            return
        }

        val timestamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        textField.append(">[$timestamp] ${data.trimEnd('\n')}\n")
    }
}