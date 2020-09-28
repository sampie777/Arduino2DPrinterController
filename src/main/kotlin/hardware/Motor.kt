package hardware


import java.util.logging.Logger

class Motor(
    val name: String,
    val mmStepDelay: Double,
    val targetReachedIdentifier: String
) {
    private val logger = Logger.getLogger(Motor::class.java.name)

    @Volatile
    var position: Double = 0.0

    @Volatile
    var targetReached: Boolean = true
}