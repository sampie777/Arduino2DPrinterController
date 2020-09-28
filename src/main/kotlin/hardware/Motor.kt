package hardware


import java.util.logging.Logger
import kotlin.math.round

class Motor(
    val name: String,
    val mmStepDelay: Double,
    val targetReachedIdentifier: String,
    val minimumStepDistance: Double
) {
    private val logger = Logger.getLogger(Motor::class.java.name)

    @Volatile
    var position: Double = 0.0

    @Volatile
    var targetReached: Boolean = true

    fun roundToMinimumDistance(position: Double): Double {
        val factor = round(position / minimumStepDistance)
        return factor * minimumStepDistance
    }
}