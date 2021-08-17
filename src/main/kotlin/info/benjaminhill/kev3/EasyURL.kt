package info.benjaminhill.kev3

import lejos.robotics.geometry.Point2D
import mu.KotlinLogging
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream


/**
 * Helpers to make working with loading URLs a bit easier.
 */

private val LOG = KotlinLogging.logger {}

typealias EasyURL = URL

fun EasyURL.delete(): Int {
    LOG.debug { "DELETE:$this" }
    val connection: HttpURLConnection = this.openConnection() as HttpURLConnection
    connection.requestMethod = "DELETE"
    return connection.responseCode
}

fun EasyURL.readTextGZ() = (openConnection() as HttpURLConnection).let { con ->
    con.setRequestProperty("Accept-Encoding", "gzip")
    if ("gzip" == con.contentEncoding) {
        LOG.debug { "Able to read GZIP content from '${this}'" }
        InputStreamReader(GZIPInputStream(con.inputStream))
    } else {
        LOG.debug { "No GZIP, fallback to plain content from '${this}'" }
        InputStreamReader(con.inputStream)
    }
}.readLines()

/**
 * Given a URL to a script of xCoordinate,yCoordinate\n...
 * @return a flow of key points to seek
 *
 * Short term: grab the whole script
 * Long term: handle huge scripts (flow)
 */
fun EasyURL.readTSV(): List<Point2D.Double> = this.readTextGZ()
    .map { it.trim() }
    .filter { line ->
        line.isNotBlank() &&
                !line.startsWith('#') &&
                line.contains('t')

    }
    .mapNotNull {
        try {
            val (x, y) = it.split(',')
            Point2D.Double(x.toDouble(), y.toDouble())
        } catch (e: NumberFormatException) {
            LOG.warn { "Skipping bad script line parse: $it $e" }
            null
        }
    }

//operator fun Point2D.component1() = this.x
//operator fun Point2D.component2() = this.y

