package info.benjaminhill.kev3

import kotlinx.coroutines.runBlocking
import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.motor.EV3MediumRegulatedMotor
import lejos.hardware.port.MotorPort
import lejos.robotics.RegulatedMotor
import mu.KotlinLogging
import kotlin.time.measureTime

private val LOG = KotlinLogging.logger {}

open class MotorWrapper : AutoCloseable {
    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                close()
            }
        })
        LOG.info { "MotorWrapper shutdown hook registered." }
    }

    protected val motorADelegate: Lazy<RegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.A).also {
            LOG.info { "Connected to motor:A" }
        }
    }

    protected val motorBDelegate: Lazy<RegulatedMotor> = lazy {
        EV3MediumRegulatedMotor(MotorPort.B).also {
            LOG.info { "Connected to motor:B" }
        }
    }

    protected val motorCDelegate: Lazy<RegulatedMotor> = lazy {
        EV3MediumRegulatedMotor(MotorPort.C).also {
            LOG.info { "Connected to motor:C" }
        }
    }

    protected val motorDDelegate: Lazy<RegulatedMotor> = lazy {
        EV3LargeRegulatedMotor(MotorPort.D).also {
            LOG.info { "Connected to motor:D" }
        }
    }

    override fun close() {
        val shutdownTime = measureTime {
            for (motorDel in listOf(motorADelegate, motorBDelegate, motorCDelegate, motorDDelegate)) {
                if (motorDel.isInitialized()) {
                    LOG.warn { "Closing ${motorDel.value}" }
                    motorDel.value.flt(true)
                    motorDel.value.close()
                } else {
                    LOG.info { "Shutdown skipping motor ${motorDel}." }
                }
            }
        }
        println("Graceful shut down:end in $shutdownTime")
    }
}