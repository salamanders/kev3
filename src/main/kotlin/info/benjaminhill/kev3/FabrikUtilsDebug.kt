package info.benjaminhill.kev3

import au.edu.federation.caliko.FabrikBone2D
import au.edu.federation.caliko.FabrikChain2D
import au.edu.federation.caliko.FabrikStructure2D
import au.edu.federation.utils.Vec2f
import mu.KotlinLogging
import java.io.File

private val LOG = KotlinLogging.logger {}

val UP = Vec2f(0f, 1f)

fun FabrikStructure2D.chains(): List<FabrikChain2D> = (0 until numChains).map { getChain(it) }
fun FabrikChain2D.bones(): List<FabrikBone2D> = (0 until numBones).map { getBone((it)) }

fun FabrikStructure2D.debugLog() {
    chains().forEachIndexed { ci, chain ->
        LOG.info { "# Chain $ci" }
        chain.bones().forEachIndexed { bi, bone ->
            LOG.info { "## Chain $ci Bone $bi" }
            LOG.info { " * endLocation:  -> ${bone.endLocation}" }
            LOG.info { " * dir: ${bone.directionUV}" }
            LOG.info { " * angle:${bone.directionUV.getSignedAngleDegsTo(UP)}" }
            if (bi > 0) {
                LOG.info { " * angle from previous2: ${chain.getBone(bi).directionUV.getSignedAngleDegsTo(chain.getBone(bi - 1).directionUV)}" }
            }

        }
    }
}

fun FabrikStructure2D.debugSVG(
    target: Vec2f? = getChain(0).lastTargetLocation
) {
    File("debug.svg").printWriter().use { pw ->
        pw.println("""<svg viewBox="-100 -100 200 200" xmlns="http://www.w3.org/2000/svg">""")
        chains().forEach { chain ->
            val chainStart = chain.bones().first().startLocation!!
            pw.println(""" <circle cx="${chainStart.x}" cy="${chainStart.y * -1}" r="3" fill="green" fill-opacity="0.5"/>""")
            val chainEnd = chain.bones().last().endLocation!!
            pw.println(""" <circle cx="${chainEnd.x}" cy="${chainEnd.y * -1}" r="3" fill="red" fill-opacity="0.5"/>""")

            chain.bones().forEach { bone ->
                pw.println(""" <line x1="${bone.startLocation.x}" y1="${bone.startLocation.y * -1}" x2="${bone.endLocation.x}" y2="${bone.endLocation.y * -1}" stroke="black" />""")
            }
        }
        target?.let {
            pw.println(""" <circle cx="${it.x}" cy="${it.y * -1}" r="3" fill="blue" fill-opacity="0.5"/>""")
        }
        pw.println("</svg>")
    }
}
