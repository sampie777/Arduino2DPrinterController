package gui


import java.util.logging.Logger
import javax.swing.JFrame

class MainFrame : JFrame() {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    companion object {
        private var instance: MainFrame? = null
        fun getInstance() = instance

        fun create(): MainFrame = MainFrame()

        fun createAndShow(): MainFrame {
            val frame = create()
            frame.isVisible = true
            return frame
        }
    }

    init {
        instance = this

        addWindowListener(MainFrameWindowAdapter(this))

        initGUI()
    }

    private fun initGUI() {
        add(MainFramePanel())

//        setSize(1000, 600)
        pack()
        title = "Arduino2DPrinterController"
        defaultCloseOperation = EXIT_ON_CLOSE
    }
}