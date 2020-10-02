package config

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.logging.Logger

object Config {
    private val logger = Logger.getLogger(Config.toString())

    // Connection
    var deviceName = "ttyUSB0"
    var baudRate = 115200

    // Printer
    var resetHeadWhenShutDown: Boolean = false
    val headOffset = arrayOf(20.0, 45.0, 0.0)
    var serialStringIsBooting = "[Main] Booting."
    var serialStringCalibratingMotorX = "[StepperMotor] X: Finding reset position"
    var serialStringCalibratingMotorY = "[StepperMotor] Y: Finding reset position"
    var serialStringCalibratingMotorZ = "[StepperMotor] Z: Finding reset position"
    var serialStringBootDone = "[Main] Boot done."
    var serialStringSweepOn = "[Serial] Toggling sweep mode on"
    var serialStringSweepOff = "[Serial] Toggling sweep mode off"
    var serialStringSweepUpX = "[Main] X: Sweep up"
    var serialStringSweepDownX = "[Main] X: Sweep down"
    var serialStringSweepUpY = "[Main] Y: Sweep up"
    var serialStringSweepDownY = "[Main] Y: Sweep down"
    var serialStringSweepUpZ = "[Main] Z: Sweep up"
    var serialStringSweepDownZ = "[Main] Z: Sweep down"
    var serialStringMotorXTargetReached = "[StepperMotor] X: Target reached"
    var serialStringMotorYTargetReached = "[StepperMotor] Y: Target reached"
    var serialStringMotorZTargetReached = "[StepperMotor] Z: Target reached"
    var serialStringCalibrateMotors = "f"
    var serialStringTurnSweepOn = "s1"
    var serialStringTurnSweepOff = "s0"

    // Runtime
    var runVirtual = false
    var runVirtualSpeed: Long = 4

    // GUI
    var paintFPS: Long = 25
    var pixelsPerMm = 15.0
    var maxLastKnownPositions = 1000
    var displayCoordinatesSendInTerminal = true

    fun load() {
        try {
            PropertyLoader.load()
            PropertyLoader.loadConfig(this::class.java)
        } catch (e: Exception) {
            logger.severe("Failed to load Config")
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            if (PropertyLoader.saveConfig(this::class.java)) {
                PropertyLoader.save()
            }
        } catch (e: Exception) {
            logger.severe("Failed to save Config")
            e.printStackTrace()
        }
    }

    fun get(key: String): Any? {
        try {
            return javaClass.getDeclaredField(key).get(this)
        } catch (e: Exception) {
            logger.severe("Could not get config key $key")
            e.printStackTrace()
        }
        return null
    }

    fun set(key: String, value: Any?) {
        try {
            javaClass.getDeclaredField(key).set(this, value)
        } catch (e: Exception) {
            logger.severe("Could not set config key $key")
            e.printStackTrace()
        }
    }

    fun enableWriteToFile(value: Boolean) {
        PropertyLoader.writeToFile = value
    }

    fun fields(): List<Field> {
        val fields = javaClass.declaredFields.filter {
            it.name != "INSTANCE" && it.name != "logger"
                    && Modifier.isStatic(it.modifiers)
        }
        fields.forEach { it.isAccessible = true }
        return fields
    }
}