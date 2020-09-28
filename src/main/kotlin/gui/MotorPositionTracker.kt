package gui


import hardware.Printer
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.util.logging.Logger
import javax.swing.JComponent
import javax.swing.border.LineBorder
import kotlin.math.roundToInt

class MotorPositionTracker : JComponent() {
    private val logger = Logger.getLogger(MotorPositionTracker::class.java.name)

    private val pixelsPerMm: Double = 10.0
    private val lastKnownPositions = arrayListOf<Array<Double>>()
    private val maxLastKnownPositions = 400

    init {
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

        while (lastKnownPositions.size > maxLastKnownPositions) {
            lastKnownPositions.removeAt(0)
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponents(g as Graphics2D)
        setDefaultRenderingHints(g)

        saveLastKnownPositions()

        g.color = Color(83, 83, 83)
        g.fillRect(0, 0, width, height)

        lastKnownPositions.forEachIndexed { index, it ->
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
    }
}