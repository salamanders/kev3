import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import lejos.robotics.geometry.Point2D
import mu.KotlinLogging
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

val LOG = KotlinLogging.logger {}

/**
 * Given a URL to a script of xCoordinate,yCoordinate\n...
 * @return a flow of key points to seek
 */
fun URL.toScript(): Flow<Point2D.Double> {
    val con = openConnection() as HttpURLConnection
    con.setRequestProperty("Accept-Encoding", "gzip")
    if ("gzip" == con.contentEncoding) {
        LOG.debug { "Able to read GZIP content from '${this@toScript}'" }
        InputStreamReader(GZIPInputStream(con.inputStream))
    } else {
        LOG.debug { "No GZIP, fallback to plain content from '${this@toScript}'" }
        InputStreamReader(con.inputStream)
    }.use { isr ->
        return isr.buffered().lineSequence()
            .map {
                val (x, y) = it.split(',')
                Point2D.Double(x.toDouble(), y.toDouble())
            }
            .asFlow().flowOn(Dispatchers.IO)
    }
}

operator fun Point2D.component1() = this.x
 operator fun Point2D.component2() = this.y

