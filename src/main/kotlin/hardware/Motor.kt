package hardware


import java.util.logging.Logger
import kotlin.math.round

class Motor(
    val name: String,
    val targetReachedIdentifier: String,
    val minimumStepDistance: Double,    // Calculated by: 1 / (maxSteps / maxDistance)
) {
    private val logger = Logger.getLogger(Motor::class.java.name)

    @Volatile
    var position: Double = -0.01
    @Volatile
    var target: Double = 0.0

    @Volatile
    var targetReached: Boolean = true

    fun setTargetPosition(value: Double) {
        target = value
        targetReached = position == target

        if (name == "Z") {
            Thread.sleep(10)
        }
    }

    fun roundToMinimumDistance(position: Double): Double {
        val factor = round(position / minimumStepDistance)
        return factor * minimumStepDistance
    }
}