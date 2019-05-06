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
import com.google.inject.Binding
import com.google.inject.Key
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.emptyImmutableList
import javax.inject.Inject

internal class ScriptIntegrationTest {

    @Suppress("NestedLambdaShadowedImplicitParameter")
    private data class TestClass
    @Inject constructor(
        val bowlerDeviceFactory: BowlerDeviceFactory,
        val resourceFactory: UnprovisionedDeviceResourceFactory
    ) {
        init {
            val device = bowlerDeviceFactory.makeBowlerDevice(
                DeviceId(
                    DefaultDeviceTypes.UnknownDevice,
                    DefaultConnectionMethods.RawHID(0, 0)
                ),
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

            val unprovisionedDigitalOut = resourceFactory.makeUnprovisionedDigitalOut(
                device,
                DefaultAttachmentPoints.Pin(7)
            ).fold({ fail { "" } }, { it })

            device.add(unprovisionedDigitalOut)

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

        script.addToInjector(Script.getDefaultModules())
        script.startScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    @Test
    fun `register hardware inside a groovy script`() {
        val scriptText =
            """
            import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDigitalOutFactory
            import com.neuronrobotics.bowlerkernel.scripting.MockBowlerRPCProtocol

            import javax.inject.Inject

            class Test {

                boolean worked = false

                @Inject
                Test(BowlerDeviceFactory bowlerDeviceFactory,
                     UnprovisionedDigitalOutFactory resourceFactory
                ) {
                    bowlerDeviceFactory.makeBowlerDevice(
                            new DeviceId(
                                    new DefaultDeviceTypes.UnknownDevice(),
                                    new DefaultConnectionMethods.RawHID(0, 0)
                            ),
                            new MockBowlerRPCProtocol()
                    ).map {
                        it.connect()

                        resourceFactory.makeUnprovisionedDigitalOut(
                                it,
                                new DefaultAttachmentPoints.Pin(1 as byte)
                        ).map { led1 ->
                            worked = true
                            it.add(led1)
                        }

                        it.disconnect()
                    }
                }
            }

            return injector.getInstance(Test).worked
            """.trimIndent()

        val script = DefaultScriptFactory(
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

        script.addToInjector(Script.getDefaultModules())

        // Run the script a first time, this should work fine
        script.startScript(emptyImmutableList()).bimap(
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
        script.startScript(emptyImmutableList()).bimap(
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
        script.startScript(emptyImmutableList()).bimap(
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
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
            import com.neuronrobotics.bowlerkernel.scripting.MockBowlerRPCProtocol
            import javax.inject.Inject

            class MyScript
            @Inject constructor(
                val bowlerDeviceFactory: BowlerDeviceFactory,
                val resourceFactory: UnprovisionedDeviceResourceFactory
            ) : Script() {

                var worked = false

                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    bowlerDeviceFactory.makeBowlerDevice(
                        DeviceId(
                            DefaultDeviceTypes.UnknownDevice,
                            DefaultConnectionMethods.RawHID(0, 0)
                        ),
                        MockBowlerRPCProtocol()
                    ).map {
                        it.connect()

                        val led1 = resourceFactory.makeUnprovisionedDigitalOut(
                            it,
                            DefaultAttachmentPoints.Pin(1)
                        ).fold(
                            { throw IllegalStateException("") },
                            {
                                worked = true
                                it
                            }
                        )

                        it.add(led1)
                        it.disconnect()
                    }
                    return Either.right(worked)
                }

                override fun stopScript() {
                }
            }

            MyScript::class
            """.trimIndent()

        val script = DefaultScriptFactory(
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

        script.addToInjector(Script.getDefaultModules())

        // Run the script a first time, this should work fine
        script.startScript(emptyImmutableList()).bimap(
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
        script.startScript(emptyImmutableList()).bimap(
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
        script.startScript(emptyImmutableList()).bimap(
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
    fun `test injector adds existing modules`() {
        val scriptText =
            """
            import arrow.core.Either
            import arrow.core.right
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script

            class TestScript : Script() {
                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    return injector.allBindings.right()
                }

                override fun stopScript() {
                }
            }

            TestScript::class
            """.trimIndent()

        val script = DefaultScriptFactory(
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

        script.addToInjector(module {
            bind<IFoo>().to<Foo>()
        })

        // Run the script a first time, this should work fine
        script.startScript(emptyImmutableList()).bimap(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            {
                @Suppress("UNCHECKED_CAST")
                val bindings = it as Map<Key<*>, Binding<*>>

                assertThat(
                    bindings.keys.filter { it.typeLiteral.rawType == IFoo::class.java },
                    hasSize(equalTo(1))
                )
            }
        )
    }

    @Test
    fun `test stopAndCleanUp is called`() {
        val scriptText =
            """
            import arrow.core.Either
            import arrow.core.right
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script

            class TestScript : Script() {

                private val counter = mutableListOf(1)

                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    return counter.right()
                }

                override fun stopScript() {
                    counter.add(2)
                }
            }

            TestScript::class
            """.trimIndent()

        val script = DefaultScriptFactory(
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
        @Suppress("UNCHECKED_CAST")
        val result = script.startScript(emptyImmutableList()).fold(
            {
                fail {
                    """
                    |Failed to run script:
                    |$it
                    """.trimMargin()
                }
            },
            {
                it as List<Int>
            }
        )

        assertIterableEquals(result, listOf(1))

        script.stopAndCleanUp()

        assertIterableEquals(result, listOf(1, 2))
    }

    private interface IFoo
    private class Foo : IFoo
}
