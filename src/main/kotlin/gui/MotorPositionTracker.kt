package gui


import config.Config
import events.EventsHub
import events.PrinterEventListener
import hardware.Printer
import hardware.PrinterState
import java.awt.*
import java.util.*
import java.util.logging.Logger
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.LineBorder
import kotlin.math.roundToInt

class MotorPositionTracker : JPanel(), PrinterEventListener {
    private val logger = Logger.getLogger(MotorPositionTracker::class.java.name)

    private val pixelsPerMm: Double = Config.pixelsPerMm
    private val lastKnownPositions = arrayListOf<Array<Double>>()
    private val showPrintingStates = arrayOf(
        PrinterState.NOT_CONNECTED,
        PrinterState.BOOTING,
        PrinterState.CALIBRATING,
        PrinterState.SWEEPING,
        PrinterState.IDLE,
    )

    private val screenUpdateTimer = Timer()
    @Volatile
    private var isRepainting = false

    init {
        initGui()

        EventsHub.register(this)

        startScreenUpdateTimer()
    }

    private fun initGui() {
        border = LineBorder(Color.BLACK)
        preferredSize = Dimension((109 * pixelsPerMm).toInt(), (31 * pixelsPerMm).toInt())
    }

    private fun saveLastKnownPositions() {
        val position = arrayOf(Printer.motorX.position, Printer.motorY.position)

        val positionExists = lastKnownPositions.find { it[0] == position[0] && it[1] == position[1] }
        if (positionExists != null) {
            return
        }

        lastKnownPositions.add(position)

        while (lastKnownPositions.size > Config.maxLastKnownPositions) {
            lastKnownPositions.removeAt(0)
        }
    }

    override fun targetReached(x: Double, y: Double) {
        saveLastKnownPositions()
    }

    override fun paintComponent(g: Graphics) {
        isRepainting = true

        super.paintComponents(g as Graphics2D)
        setDefaultRenderingHints(g)

        g.color = Color(83, 83, 83)
        g.fillRect(0, 0, width, height)

        lastKnownPositions
            .toTypedArray()
            .forEachIndexed { index, it ->
                val positionX = it[0] * pixelsPerMm
                val positionY = it[1] * pixelsPerMm

                g.color = Color(200 - (200 * index.toDouble() / lastKnownPositions.size).roundToInt(), 255, 0)
                g.fillOval(positionX.roundToInt() - 1, positionY.roundToInt() - 1, 2, 2)
            }

        val positionX = Printer.motorX.position * pixelsPerMm
        val positionY = Printer.motorY.position * pixelsPerMm
        g.color = Color.GREEN
        g.fillOval(positionX.roundToInt() - 2, positionY.roundToInt() - 2, 5, 5)

        val targetX = Printer.motorX.target * pixelsPerMm
        val targetY = Printer.motorY.target * pixelsPerMm
        g.color = Color.RED
        g.fillOval(targetX.roundToInt() - 3, targetY.roundToInt() - 3, 7, 7)

        if (Printer.state !in showPrintingStates) {
            isRepainting = false
            return
        }

        g.font = Font("Dialog", Font.PLAIN, 30)
        val message = Printer.state.name.replace("_", " ")
        val textWidth = g.fontMetrics.stringWidth(message)
        val textHeight = g.fontMetrics.height
        val textMarginWidth = 30
        val textMarginHeight = 10

        g.color = Color(56, 56, 56)
        g.fillRect(
            (width - textWidth) / 2 - textMarginWidth,
            (height - textHeight) / 2 - textMarginHeight,
            textWidth + 2 * textMarginWidth,
            textHeight + 2 * textMarginHeight,
        )
        g.color = Color.WHITE
        g.drawString(message, (width - textWidth) / 2, ((height + textHeight * 0.7) / 2).roundToInt())

        isRepainting = false
    }

    private fun startScreenUpdateTimer() {
        screenUpdateTimer.schedule(object : TimerTask() {
            override fun run() {
                if (isRepainting) {
                    logger.finer("Skipping paint update: still updating")
                    return
                }

                repaint()
            }
        }, 0, 1000 / Config.paintFPS)
    }

    fun stopScreenUpdateTimer() {
        screenUpdateTimer.cancel()
    }
}