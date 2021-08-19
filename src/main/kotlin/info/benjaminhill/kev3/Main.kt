package info.benjaminhill.kev3

import info.benjaminhill.utils.printDeepStackTrace
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.runBlocking
import lejos.robotics.geometry.Point2D
import lejos.robotics.geometry.Rectangle2D
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

private val LOG = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) = try {
    LOG.info { "Hello kev3bot!" }
    if (args.isNotEmpty()) {
        LOG.info { "args: ${args.joinToString()}" }
    }
    DrawingArms().use { arms ->
        arms.calibrate()
        // center, then trace the boundaries
        arms.moveTo(.5f, .5f)
        arms.moveTo(0f, 0f)
        arms.moveTo(0f, 1f)
        arms.moveTo(1f, 1f)
        arms.moveTo(1f, 0f)

        runBlocking {
            getLatestScript().collectIndexed { _, point: Point2D ->
                try {
                    arms.moveTo(x = point.x.toFloat(), y = point.y.toFloat())
                } catch (e: IllegalArgumentException) {
                    LOG.warn { "Ignoring illegal point: $point" }
                }
            }
        }
    }
    LOG.error { "Should never be done with script! How did we get here?" }

} catch (t: Throwable) {
    LOG.error { "Caught top level exception" }
    t.printDeepStackTrace()
}

fun Rectangle2D.str() = "{x:$x, y:$y, w:$width, h:$height}"


