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
@file:SuppressWarnings("LongMethod", "LargeClass")

package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocolFactory
import com.neuronrobotics.bowlerkernel.scripting.factory.DefaultScriptFactory
import com.neuronrobotics.bowlerkernel.scripting.parser.DefaultScriptLanguageParser
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.util.concurrent.TimeUnit

@Timeout(value = 15, unit = TimeUnit.SECONDS)
internal class ScriptIntegrationTest {

    @Suppress("NestedLambdaShadowedImplicitParameter")
    private data class TestClass(
        val bowlerDeviceFactory: BowlerDeviceFactory,
        val resourceFactory: UnprovisionedDeviceResourceFactory
    ) {
        init {
            val device = bowlerDeviceFactory.makeBowlerDevice(
                DeviceId(
                    DefaultDeviceTypes.UnknownDevice,
                    DefaultConnectionMethods.RawHID(0, 0)
                ),
                mockBowlerRPCProtocol()
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
                val bowlerDeviceFactory = DeviceFactory(
                    hardwareRegistry,
                    DefaultResourceIdValidator(),
                    SimplePacketComsProtocolFactory(
                        DefaultResourceIdValidator()
                    )
                )

                val resourceFactory = UnprovisionedDeviceResourceFactory(hardwareRegistry)

                val device = bowlerDeviceFactory.makeBowlerDevice(
                    DeviceId(
                        DefaultDeviceTypes.UnknownDevice,
                        DefaultConnectionMethods.RawHID(0, 0)
                    ),
                    mockBowlerRPCProtocol()
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

                return Either.right(null)
            }

            @SuppressWarnings("EmptyFunctionBlock")
            override fun stopScript() {
            }
        }

        script.startScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    @Test
    fun `register hardware inside a groovy script`() {
        val scriptText =
            """
            import arrow.core.Either
            import com.google.common.collect.ImmutableList
            import com.neuronrobotics.bowlerkernel.hardware.Script
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
            import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
            import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
            import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocolFactory
            import org.jetbrains.annotations.NotNull

            import static com.neuronrobotics.bowlerkernel.scripting.TestUtilKt.mockBowlerRPCProtocol

            class Test extends Script {

                boolean worked = false

                @Override
                protected Either<String, Object> runScript(@NotNull ImmutableList<Object> args) {
                    def deviceFactory = new DeviceFactory(
                            hardwareRegistry,
                            new DefaultResourceIdValidator(),
                            new SimplePacketComsProtocolFactory(new DefaultResourceIdValidator())
                    )

                    def resourceFactory = new UnprovisionedDeviceResourceFactory(hardwareRegistry)

                    deviceFactory.makeBowlerDevice(
                            new DeviceId(
                                    new DefaultDeviceTypes.UnknownDevice(),
                                    new DefaultConnectionMethods.RawHID(0, 0)
                            ),
                            mockBowlerRPCProtocol()
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

                    return new Either.Right(worked)
                }

                @Override
                protected void stopScript() {
                }
            }

            return Test
            """.trimIndent()

        val script = DefaultScriptFactory(
            DefaultScriptLanguageParser()
        ).createScriptFromText("groovy", scriptText).fold(
            { failedToCreateScript(it) },
            { it }
        )

        // Run the script a first time, this should work fine
        script.startScript(emptyImmutableList()).bimap(
            { failedToRunScript(it) },
            { assertTrue(it as Boolean) }
        )

        script.stopAndCleanUp()

        // Run the script a second time, this should work again because the script was stopped and
        // clean up after
        script.startScript(emptyImmutableList()).bimap(
            { failedToRunScript(it) },
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
            import com.neuronrobotics.bowlerkernel.scripting.mockBowlerRPCProtocol
            import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
            import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
            import com.neuronrobotics.bowlerkernel.hardware.protocol.SimplePacketComsProtocolFactory

            class MyScript : Script() {

                var worked = false

                override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                    val deviceFactory = DeviceFactory(
                        hardwareRegistry,
                        DefaultResourceIdValidator(),
                        SimplePacketComsProtocolFactory(DefaultResourceIdValidator())
                    )

                    val resourceFactory = UnprovisionedDeviceResourceFactory(hardwareRegistry)

                    deviceFactory.makeBowlerDevice(
                        DeviceId(
                            DefaultDeviceTypes.UnknownDevice,
                            DefaultConnectionMethods.RawHID(0, 0)
                        ),
                        mockBowlerRPCProtocol()
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
            { failedToCreateScript(it) },
            { it }
        )

        // Run the script a first time, this should work fine
        script.startScript(emptyImmutableList()).bimap(
            { failedToRunScript(it) },
            { assertTrue(it as Boolean) }
        )

        script.stopAndCleanUp()

        // Run the script a second time, this should work again because the script was stopped and
        // clean up after
        script.startScript(emptyImmutableList()).bimap(
            { failedToRunScript(it) },
            { assertTrue(it as Boolean) }
        )

        script.stopAndCleanUp()
    }

    @Test
    fun `test script cannot be its own parent`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                return Unit.right()
            }

            override fun stopScript() = Unit
        }

        assertThrows<IllegalArgumentException> {
            script.startScript(emptyImmutableList(), script)
        }
    }

    @Test
    fun `test script cannot be started while already running`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                return Unit.right()
            }

            override fun stopScript() = Unit
        }

        script.startScript(emptyImmutableList())

        assertThrows<IllegalArgumentException> {
            script.startScript(emptyImmutableList())
        }
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
            { failedToCreateScript(it) },
            { it }
        )

        // Run the script a first time, this should work fine
        @Suppress("UNCHECKED_CAST")
        val result = script.startScript(emptyImmutableList()).fold(
            { failedToRunScript(it) },
            { it as List<Int> }
        )

        assertIterableEquals(result, listOf(1))

        script.stopAndCleanUp()

        assertIterableEquals(result, listOf(1, 2))
    }

    private fun failedToCreateScript(it: String): Nothing =
        fail {
            """
            |Failed to create script:
            |$it
            """.trimMargin()
        }

    private fun failedToRunScript(it: String): Nothing =
        fail {
            """
            |Failed to run script:
            |$it
            """.trimMargin()
        }
}
