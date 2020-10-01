import config.Config
import hardware.Printer
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("utils")

fun linspace(start: Int, stop: Int, num: Int) =
    (start..stop step (stop - start) / (num - 1)).toList()

fun exitApplication() {
    logger.info("Exiting application...")

    App.isPaused = false

    if (Config.resetHeadWhenShutDown) {
        try {
            Printer.resetHead(waitForMotors = false, ignorePause = true)
        } catch (t: Throwable) {
            logger.warning("Failed to reset printer head")
            t.printStackTrace()
        }
    }

    try {
        Printer.disconnect()
    } catch (t: Throwable) {
        logger.warning("Failed to disconnect to printer")
        t.printStackTrace()
    }

    logger.info("Shutdown finished")
}

@Throws(UnsupportedEncodingException::class)
fun getCurrentJarDirectory(caller: Any): File {
    val url = caller::class.java.protectionDomain.codeSource.location
    val jarPath = URLDecoder.decode(url.file, "UTF-8")
    return File(jarPath).parentFile
}

fun decodeURI(uri: String): String {
    return URLDecoder.decode(uri, StandardCharsets.UTF_8.name())
}

fun getReadableFileSize(file: File): String {
    return when {
        file.length() > 1024 * 1024 -> {
            val fileSize = file.length().toDouble() / (1024 * 1024)
            String.format("%.2f MB", fileSize)
        }
        file.length() > 1024 -> {
            val fileSize = file.length().toDouble() / 1024
            String.format("%.2f kB", fileSize)
        }
        else -> {
            String.format("%d bytes", file.length())
        }
    }
}

fun getFileNameWithoutExtension(file: File): String {
    return file.name.substring(0, file.name.lastIndexOf('.'))
}

fun getFileExtension(file: File): String {
    return file.name.substring(file.name.lastIndexOf('.') + 1)
}

fun Date.format(format: String): String? = SimpleDateFormat(format).format(this)