/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.getOrHandle
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultTextScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.emptyImmutableList
import javax.inject.Inject

class ScriptIntegrationTest {

    @Suppress("NestedLambdaShadowedImplicitParameter")
    private data class TestClass
    @Inject constructor(
        val bowlerDeviceFactory: BowlerDeviceFactory,
        val unprovisionedDigitalOutFactory: UnprovisionedDigitalOutFactory.Factory
    ) {
        val digitalOut: GenericDigitalOut

        init {
            val device = bowlerDeviceFactory.makeBowlerDevice(
                SimpleDeviceId("bowler-device-id"),
                MockBowlerRPCProtocol()
            ).getOrHandle {
                fail {
                    """
                    |Got a RegisterError when making the device:
                    |$it
                    """.trimMargin()
                }
            }

            device.connect()

            val unprovisionedDigitalOut = unprovisionedDigitalOutFactory.create(device)
                .makeUnprovisionedDigitalOut(DefaultAttachmentPoints.Pin(7))

            digitalOut = unprovisionedDigitalOut.fold(
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

            device.disconnect()
        }
    }

    @Test
    fun `provision LED integration test`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                injector.getInstance(key<TestClass>())
                return Either.right(null)
            }

            override fun stopScript() {
            }
        }

        script.runScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    @Test
    fun `register hardware inside a groovy script`() {
        val scriptText =
            """
            import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
            import com.neuronrobotics.bowlerkernel.scripting.MockBowlerRPCProtocol

            import javax.inject.Inject

            class Test {

                boolean worked = false

                @Inject
                Test(BowlerDeviceFactory bowlerDeviceFactory,
                     UnprovisionedDigitalOutFactory.Factory ledFactoryFactory
                ) {
                    bowlerDeviceFactory.makeBowlerDevice(
                            new SimpleDeviceId("device A"),
                            new MockBowlerRPCProtocol()
                    ).map {
                        it.connect()

                        ledFactoryFactory.create(it).makeUnprovisionedDigitalOut(
                                new DefaultAttachmentPoints.Pin(1 as byte)
                        ).bimap(
                                {
                                    print it
                                },
                                {
                                    it.provision().map {
                                        worked = true
                                    }
                                }
                        )

                        it.disconnect()
                    }
                }
            }

            return injector.getInstance(Test).worked
            """.trimIndent()

        val script = DefaultTextScriptFactory(
            DefaultScriptLanguageParser()
        ).createScriptFromText("groovy", scriptText).fold(
            {
                fail {
                    """
                    |Failed to create script:
                    |$it
                    """.trimMargin()
                }
            },
            { it }
        )

        // Run the script a first time, this should work fine
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertTrue(it as Boolean) }
        )

        // Run the script a second time, this should fail (inside the script) because the hardware
        // has not been unregistered
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertFalse(it as Boolean) }
        )

        script.stopAndCleanUp()

        // Run the script a third time, this should work again because the script was stopped and
        // clean up after
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertTrue(it as Boolean) }
        )

        script.stopAndCleanUp()
    }

    @Test
    fun `register hardware inside a kotlin script`() {
        val scriptText =
            """
            import arrow.core.Either
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script
            import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
            import com.neuronrobotics.bowlerkernel.scripting.MockBowlerRPCProtocol
            import javax.inject.Inject

            class MyScript
            @Inject constructor(
                val bowlerDeviceFactory: BowlerDeviceFactory,
                val ledFactoryFactory: UnprovisionedDigitalOutFactory.Factory
            ) : Script() {

                var worked = false

                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    bowlerDeviceFactory.makeBowlerDevice(
                        SimpleDeviceId("device A"),
                        MockBowlerRPCProtocol()
                    ).map {
                        it.connect()

                        ledFactoryFactory.create(it).makeUnprovisionedDigitalOut(
                            DefaultAttachmentPoints.Pin(1)
                        ).map {
                            it.provision().map {
                                worked = true
                            }
                        }

                        it.disconnect()
                    }
                    return Either.right(worked)
                }

                override fun stopScript() {
                }
            }

            MyScript::class
            """.trimIndent()

        val script = DefaultTextScriptFactory(
            DefaultScriptLanguageParser()
        ).createScriptFromText("kotlin", scriptText).fold(
            {
                fail {
                    """
                    |Failed to create script:
                    |$it
                    """.trimMargin()
                }
            },
            { it }
        )

        // Run the script a first time, this should work fine
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertTrue(it as Boolean) }
        )

        // Run the script a second time, this should fail (inside the script) because the hardware
        // has not been unregistered
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertFalse(it as Boolean) }
        )

        script.stopAndCleanUp()

        // Run the script a third time, this should work again because the script was stopped and
        // clean up after
        script.runScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            { assertTrue(it as Boolean) }
        )

        script.stopAndCleanUp()
    }
}
