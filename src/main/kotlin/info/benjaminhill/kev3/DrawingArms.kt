package info.benjaminhill.kev3

import au.edu.federation.caliko.FabrikBone2D
import au.edu.federation.caliko.FabrikChain2D
import au.edu.federation.caliko.FabrikStructure2D
import au.edu.federation.utils.Vec2f
import info.benjaminhill.utils.r
import lejos.hardware.Button
import lejos.robotics.RegulatedMotor
import lejos.robotics.geometry.Rectangle2D
import mu.KotlinLogging
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DrawingArms : MotorWrapper() {
    // MOTORS
    private val shoulder: RegulatedMotor by motorADelegate
    private val elbow: RegulatedMotor by motorDDelegate

    // KINEMATICS
    private val bone0: FabrikBone2D
    private val bone1: FabrikBone2D
    private val drawingArea: Rectangle2D.Float

    private val structure = FabrikStructure2D().also { str ->
        str.addChain(FabrikChain2D().also { chain ->
            chain.setFixedBaseMode(true)
            chain.baseboneConstraintType = FabrikChain2D.BaseboneConstraintType2D.GLOBAL_ABSOLUTE
            // Create a new 2D chain
            chain.addBone(FabrikBone2D(Vec2f(0f, 0f), UP, boneLengthLego0.toFloat(), 90f, 90f))
            bone0 = chain.getBone(0)
            LOG.info { "Added upper bone" }
            chain.addConsecutiveConstrainedBone(UP, boneLengthLego1.toFloat(), 160f, 160f)
            bone1 = chain.getBone(1)
            LOG.info { "Added lower bone" }
        })
        str.setFixedBaseMode(true)
        // reach far to the upper-right to find the bounds
        val maxReach = (boneLengthLego0 + boneLengthLego1).toFloat()
        str.solveForTarget(Vec2f(maxReach, maxReach))
        val upperRight = str.getChain(0).effectorLocation
        drawingArea = Rectangle2D.Float(
            -upperRight.x.roundToInt().toFloat(),
            0f,
            (upperRight.x * 2).roundToInt().toFloat(),
            upperRight.y.roundToInt().toFloat()
        )
        LOG.info { "Drawing area: ${drawingArea.str()}" }
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
        val scaledTarget = target.scaleUnitTo(drawingArea)
        LOG.info { "moveTo($target)" }
        location = scaledTarget

        //structure.debugLog()
        //structure.debugSVG()
    }

    /** Match the real world to the imagined position */
    fun calibrate() {
        shoulder.flt(true)
        elbow.flt(true)
        println("Both arms straight up, then OK")
        pressButtonsUntilPose()
        shoulder.resetTachoCount()
        elbow.resetTachoCount()
    }

    private fun pressButtonsUntilPose() {
        while (true) {
            when (Button.waitForAnyPress()) {
                Button.ID_ENTER -> {
                    return
                }
                Button.ID_UP -> {
                    shoulder.rotate(5)
                }
                Button.ID_DOWN -> {
                    shoulder.rotate(-5)
                }
                Button.ID_LEFT -> {
                    elbow.rotate(5)
                }
                Button.ID_RIGHT -> {
                    elbow.rotate(-5)
                }
                Button.ID_ESCAPE -> {
                    // Allows emergency bail
                    shoulder.flt(true)
                    elbow.flt(true)
                    throw Exception("Emergency exit from calibration.")
                }
            }
        }
    }

    companion object {
        private val LOG = KotlinLogging.logger {}

        // Units: number of lego studs
        private const val boneLengthLego0 = 41
        private const val boneLengthLego1 = 40

        private fun Vec2f.scaleUnitTo(drawingArea: Rectangle2D): Vec2f {
            require(x in 0f..1f) { "Unit Vec2f had out of bounds x:$x" }
            require(y in 0f..1f) { "Unit Vec2f had out of bounds y:$y" }
            val scaledX = drawingArea.x + (drawingArea.width * x)
            val scaledY = drawingArea.y + (drawingArea.height * y)
            return Vec2f(scaledX.toFloat(), scaledY.toFloat())
        }
    }
}

