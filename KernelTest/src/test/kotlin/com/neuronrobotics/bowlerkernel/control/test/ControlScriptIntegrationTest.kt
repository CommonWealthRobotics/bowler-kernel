package com.neuronrobotics.bowlerkernel.control.test

import arrow.core.getOrHandle
import com.neuronrobotics.bowlerkernel.control.ControlScript
import com.neuronrobotics.bowlerkernel.control.KernelOrchestrator
import com.neuronrobotics.bowlerkernel.control.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.LED
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.PinNumber
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedLEDFactory
import com.neuronrobotics.bowlerkernel.control.hardware.protocol.BowlerRPCProtocol
import com.nhaarman.mockitokotlin2.mock
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
            val mockRPC = mock<BowlerRPCProtocol> {}

            val device =
                bowlerDeviceFactory.makeBowlerDevice(SimpleDeviceId("bowler-device-id"), mockRPC)
                    .getOrHandle {
                        throw RuntimeException(it.errorString)
                    }

            val unprovisionedLED =
                unprovisionedLEDFactory.create(device).makeUnprovisionedLED(PinNumber(7))

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
