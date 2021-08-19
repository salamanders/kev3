package info.benjaminhill.kev3

import com.google.gson.Gson
import info.benjaminhill.utils.delete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lejos.robotics.geometry.Point2D
import mu.KotlinLogging
import java.net.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private val LOG = KotlinLogging.logger {}

private const val PROJECT_ID = "kev3bot"
private const val API_KEY = "AIzaSyC6_RUabvPPQ82KWc57GbXs24heXY0-sgs"

private const val FB_URL = "https://firestore.googleapis.com/v1"

private const val COLLECTION_NAME = "points"

private val GSON: Gson = Gson()

private data class ScriptPoint(
    private val x: Map<String, Double>,
    private val y: Map<String, Double>,
    private val ts: Map<String, Long>,
) {
    init {
        require(x.isNotEmpty())
        require(x.values.first() in 0.0..1.0)
        require(y.isNotEmpty())
        require(y.values.first() in 0.0..1.0)
    }

    fun getX() = x.values.first()
    fun getY() = y.values.first()
    fun getTS() = ts.values.first()
    override fun toString(): String {
        return listOf(getX(), getY(), getTS()).joinToString("\t")
    }
}

private data class FirestoreDocumentPoint(
    val name: String,
    //private val createTime:String, // TODO(optimization): Filter these out
    //private val updateTime:String,
    private val fields: ScriptPoint,
) {
    fun point() = fields
}

private data class FirestoreReadResponse(
    val documents: List<FirestoreDocumentPoint>?,
    val nextPageToken: String?,
)

@OptIn(ExperimentalTime::class)
fun getLatestScript(): Flow<Point2D> = flow {
    while (true) {
        do {
            val readAllUrl = "$FB_URL/projects/$PROJECT_ID/databases/(default)" +
                    "/documents/$COLLECTION_NAME?key=$API_KEY&pageSize=100&orderBy=ts"
            val rawText = URL(readAllUrl).readText()
            println("Raw Text: $rawText")
            val response = GSON.fromJson(rawText, FirestoreReadResponse::class.java)
            response?.documents?.forEach { document ->
                emit(document.point().let {
                    Point2D.Double(it.getX(), it.getY())
                })
                URL("$FB_URL/${document.name}?key=$API_KEY").delete()
            }
        } while (response.nextPageToken != null)
        LOG.info { "End of points, pausing before checking for more." }
        delay(Duration.seconds(10))
    }
}.flowOn(Dispatchers.IO).buffer(capacity = 25, onBufferOverflow = BufferOverflow.SUSPEND)