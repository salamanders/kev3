package info.benjaminhill.kev3

import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.runBlocking
import lejos.robotics.geometry.Point2D
import mu.KotlinLogging
import java.io.IOException
import kotlin.time.ExperimentalTime

private val LOG = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) = try {
    LOG.info { "Hello kev3bot!" }
    if (args.isNotEmpty()) {
        LOG.info { "args: ${args.joinToString()}" }
    }
    Arms.calibrate()

    Arms.moveTo(.5f, .5f)
    Arms.moveTo(0f, 0f)
    Arms.moveTo(0f, 1f)
    Arms.moveTo(1f, 1f)
    Arms.moveTo(1f, 0f)

    runBlocking {
        getLatestScript().collectIndexed { index, point: Point2D ->
            println(point)
        }
    }
    LOG.error { "Should never be done with script! How did we get here?" }
} catch (t: Throwable) {
    debugAnyError("Caught top level exception", t)
}


fun debugAnyError(message: String, th: Throwable?) {
    val logMessage = "[ERROR] - $message"
    try {
        println(logMessage)
        if (th == null) {
            return
        }
        th.stackTrace?.also {
            println("${th.javaClass}: ${th.message}")
        }?.forEach { trace ->
            println("  at ${trace.className}.${trace.methodName} (${trace.fileName}:${trace.lineNumber})")
        }
        var cause = th.cause
        while (null != cause) {
            cause.stackTrace?.also {
                println("Caused By: ${cause?.javaClass}: ${cause?.message}")
            }?.forEach { stackTraceElement ->
                println("  at ${stackTraceElement.className}.${stackTraceElement.methodName} (${stackTraceElement.fileName}:${stackTraceElement.lineNumber}")
            }
            cause = cause.cause
        }
    } catch (ex: IOException) {
        System.err.println(logMessage)
        th?.printStackTrace()
    }
}