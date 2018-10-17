package com.neuronrobotics.bowlerkernel.control.test

import com.neuronrobotics.bowlerkernel.control.ControlScript
import com.neuronrobotics.bowlerkernel.control.KernelOrchestrator
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.LED
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedLEDFactory
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import javax.inject.Inject

class ControlScriptIntegrationTest {

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
    fun `provision LED integration test`() {
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
