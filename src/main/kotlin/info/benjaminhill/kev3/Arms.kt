package info.benjaminhill.kev3


import au.edu.federation.caliko.FabrikBone2D
import au.edu.federation.caliko.FabrikChain2D
import au.edu.federation.caliko.FabrikStructure2D
import au.edu.federation.utils.Vec2f
import kotlinx.coroutines.runBlocking
import lejos.hardware.Button
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.port.MotorPort
import lejos.robotics.RegulatedMotor
import lejos.robotics.geometry.Rectangle2D
import mu.KotlinLogging
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private val LOG = KotlinLogging.logger {}

private const val boneLengthLego0 = 41
private const val boneLengthLego1 = 40

@ExperimentalTime
object Arms {
    // MOTORS
    /** Keep the delegate for making shutdown/close optional. */
    private val shoulderDelegate: Lazy<EV3LargeRegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.A).also {
            LOG.info { "Connected to shoulder:A" }
        }
    }
    private val shoulder: RegulatedMotor by shoulderDelegate

    /** Keep the delegate for making shutdown/close optional. */
    private val elbowDelegate: Lazy<EV3LargeRegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.D).also {
            LOG.info { "Connected to elbow:D" }
        }
    }
    private val elbow: RegulatedMotor by elbowDelegate


    // KINEMATICS
    private val bone0: FabrikBone2D
    private val bone1: FabrikBone2D
    private val drawingArea: Rectangle2D.Float

    // Required?
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
            LOG.info { "  location.set($targetLocation) solved to $location with dist:$dist" }
        }

    fun moveTo(x: Float, y: Float) {
        require(x in 0f..1f) { "moveTo bad x:$x" }
        require(y in 0f..1f) { "moveTo bad y:$y" }
        val scaledX = x * drawingArea.width - drawingArea.x
        val scaledY = y * drawingArea.height - drawingArea.y
        val scaledTarget = Vec2f(scaledX, scaledY)
        LOG.info { "moveTo($x, $y), scaled to:$scaledTarget" }
        location = scaledTarget
        LOG.info {
            "moveTo: ${structure.getChain(0).lastTargetLocation}, solved to:${location}"
        }
        structure.debugLog()
        structure.debugSVG()

    }

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                val shutdownTime = measureTime {
                    LOG.warn { "Graceful shut down:start" }
                    if (shoulderDelegate.isInitialized()) {
                        LOG.warn { "Closing Shoulder" }
                        shoulder.flt(true)
                        shoulder.close()
                    } else {
                        LOG.info { "Shoulder was never opened." }
                    }
                    if (elbowDelegate.isInitialized()) {
                        LOG.warn { "Closing Elbow" }
                        elbow.flt(true)
                        elbow.close()
                    } else {
                        LOG.info { "Elbow was never opened." }
                    }
                }
                println("Graceful shut down:end in $shutdownTime")
            }
        })
        LOG.info { "Motor shutdown hook registered." }
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
                    throw Exception("Bailed on calibration.")
                }
            }
        }
    }
}
