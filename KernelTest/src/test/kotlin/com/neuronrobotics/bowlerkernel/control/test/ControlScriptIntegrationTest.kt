/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
import org.junit.jupiter.api.fail
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
                        fail {
                            """
                            |Got a RegisterError when making the device:
                            |$it
                            """.trimMargin()
                        }
                    }

            val unprovisionedLED =
                unprovisionedLEDFactory.create(device).makeUnprovisionedLED(PinNumber(7))

            led = unprovisionedLED.fold(
                {
                    fail {
                        """
                        |Got a RegisterError when making the LED:
                        |$it
                        """.trimMargin()
                    }
                },
                {
                    it.provision().fold(
                        {
                            fail {
                                """
                                |Got a ProvisionError when provisioning the LED:
                                |$it
                                """.trimMargin()
                            }
                        },
                        { it }
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

        KernelOrchestrator().apply {
            startControlScript(script)
            stopControlScript(script)
        }
    }
}
