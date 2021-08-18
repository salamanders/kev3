package info.benjaminhill.kev3

import info.benjaminhill.utils.printDeepStackTrace
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.runBlocking
import lejos.robotics.geometry.Point2D
import lejos.robotics.geometry.Rectangle2D
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
    //Arms.calibrate()

    Arms.moveTo(.3f, .3f)
/*
    Arms.moveTo(0f, 0f)
    Arms.moveTo(0f, 1f)
    Arms.moveTo(1f, 1f)
    Arms.moveTo(1f, 0f)
*/
    /*
    runBlocking {
        getLatestScript().collectIndexed { _, point: Point2D ->
            println(point)
        }
    }
    LOG.error { "Should never be done with script! How did we get here?" }

     */
} catch (t: Throwable) {
    LOG.error { "Caught top level exception" }
    t.printDeepStackTrace()
}



fun Rectangle2D.str() = "{x:$x, y:$y, w:$width, h:$height}"