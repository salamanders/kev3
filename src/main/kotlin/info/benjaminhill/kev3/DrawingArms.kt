package info.benjaminhill.kev3

import au.edu.federation.caliko.FabrikBone2D
import au.edu.federation.caliko.FabrikChain2D
import au.edu.federation.caliko.FabrikStructure2D
import au.edu.federation.utils.Vec2f
import com.google.common.primitives.Floats.min
import info.benjaminhill.utils.r
import lejos.robotics.RegulatedMotor
import lejos.robotics.geometry.Rectangle2D
import scaleUnitTo
import str
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

/** Two bones in a row, like your upper and lower arm, with a pen at the end */
@ExperimentalTime
class DrawingArms : MotorWrapper() {
    // MOTORS
    private val shoulder: RegulatedMotor by motorADelegate
    private val elbow: RegulatedMotor by motorDDelegate

    // KINEMATICS
    // Units: number of lego studs
    private val bone0: FabrikBone2D = FabrikBone2D(Vec2f(0f, 0f), UP, 41f, 90f, 90f)
    private val bone1: FabrikBone2D = FabrikBone2D(Vec2f(0f, 40f), UP, 41f, 160f, 160f)

    private val structure = FabrikStructure2D().apply {
        addChain(FabrikChain2D().apply {
            setFixedBaseMode(true)
            baseboneConstraintType = FabrikChain2D.BaseboneConstraintType2D.GLOBAL_ABSOLUTE
            addBone(bone0)
            addConsecutiveBone(bone1)
        })
        setFixedBaseMode(true)
    }

    // Should be square (ish) with the base bone starting in the center-bottom.
    private val drawingArea: Rectangle2D.Float = run {
        // reach far to the upper-right diagonal to find the bounds.
        val maxReach = (Vec2f.distanceBetween(bone0.startLocation, bone0.endLocation) +
                Vec2f.distanceBetween(bone1.startLocation, bone1.endLocation))
        structure.solveForTarget(Vec2f(maxReach, /*maximize the square */maxReach * 2))
        // direct access to location during init
        val upperRight = structure.getChain(0).effectorLocation
        val edgeLength = min(upperRight.x * 2, upperRight.y).roundToInt().toFloat()
        Rectangle2D.Float(-edgeLength / 2, 0f, edgeLength, edgeLength)
    }.also {
        LOG.info { "Drawing area: ${it.str()}" }
    }

    /** Real-world scale */
    private var location: Vec2f
        get() = structure.getChain(0).effectorLocation
        set(targetLocation) {
            // TODO: Partial moves to make the strokes linear.
            structure.solveForTarget(targetLocation)
            val dist = Vec2f.distanceBetween(location, targetLocation)
            LOG.info { "  location.set($targetLocation) solved to $location with dist:${dist.r}" }
            // TODO: Actually move the motors!
        }

    fun moveTo(x: Float, y: Float) = moveTo(Vec2f(x, y))

    private fun moveTo(target: Vec2f) {
        LOG.info { "moveTo($target)" }
        val scaledTarget = target.scaleUnitTo(drawingArea)

        val maximumNumberOfHops = (ceil(scaledTarget.minus(location).length() / MAX_HOP_DISTANCE) * 2).toInt()
        var numberOfHops = 0
        do {
            val startRemainingMove = scaledTarget.minus(location)
            numberOfHops++
            LOG.info { "  location:$location to scaledTarget:$scaledTarget requires remainingMove:$startRemainingMove hop:$numberOfHops of $maximumNumberOfHops" }
            val nextTarget = if (startRemainingMove.length() > MAX_HOP_DISTANCE) {
                location.plus(startRemainingMove.normalise().times(MAX_HOP_DISTANCE)).also {
                    LOG.info { "    taking a partial step to $it" }
                }
            } else {
                scaledTarget.also {
                    LOG.info { "    taking a final step to $it" }
                }
            }
            location = nextTarget
            if (numberOfHops >= maximumNumberOfHops) {
                LOG.warn { "Failed to reach goal within $maximumNumberOfHops hops, writing debug." }
                try {
                    structure.debugSVG()
                } catch (e: IOException) {
                    LOG.warn(e) { "Issues saving debug SVG." }
                }
            }
        } while (numberOfHops < maximumNumberOfHops && Vec2f.distanceBetween(
                location,
                scaledTarget
            ) >= MOVE_GOAL_EPSILON
        )
    }

    /** Physically move the real world to the imagined starting position */
    fun calibrate() {
        shoulder.flt(true)
        elbow.flt(true)
        println("Both arms straight up, then OK")
        pressButtonsUntilPose(shoulder, elbow)
        shoulder.resetTachoCount()
        elbow.resetTachoCount()
    }

    fun debugSVG() = structure.debugSVG()
    fun debugLog() = structure.debugLog()

    companion object {
        const val MAX_HOP_DISTANCE = 10f
        const val MOVE_GOAL_EPSILON = 3f
    }
}

