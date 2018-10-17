package com.neuronrobotics.bowlerkernel.control.test

import com.neuronrobotics.bowlerkernel.control.ControlScript
import com.neuronrobotics.bowlerkernel.control.KernelOrchestrator
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.LED
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedLEDFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import javax.inject.Inject

class KernelOrchestratorTest {

    private open class MockControlScript : ControlScript() {

        public override fun runScript() {
        }

        public override fun stopScript() {
        }
    }

    @Test
    fun `test start`() {
        val script = mock<MockControlScript> {
        }

        val orchestrator = KernelOrchestrator()

        orchestrator.startControlScript(script)
        verify(script).runScript()

        orchestrator.stopControlScript(script)
        verify(script).stopScript()
    }

    @Suppress("NestedLambdaShadowedImplicitParameter")
    private class TestClass
    @Inject constructor(
            bowlerDeviceFactory: BowlerDeviceFactory,
            unprovisionedLEDFactory: UnprovisionedLEDFactory.Factory
    ) {
        val led: LED

        init {
            val device = bowlerDeviceFactory.makeBowlerDevice("bowler-device-id")
            val unprovisionedLED = unprovisionedLEDFactory.create(device).makeUnprovisionedLED(7)
            led = unprovisionedLED.fold(
                    {
                        throw RuntimeException(it.errorString)
                    },
                    {
                        it.provision().fold(
                                {
                                    throw RuntimeException(it.errorString)
                                },
                                {
                                    it
                                }
                        )
                    }
            )
        }
    }

    @Test
    fun `lel help`() {
        val script = object : ControlScript() {
            override fun runScript() {
                injector.getInstance(key<TestClass>())
            }

            override fun stopScript() {
            }
        }

        KernelOrchestrator().startControlScript(script)
    }
}
