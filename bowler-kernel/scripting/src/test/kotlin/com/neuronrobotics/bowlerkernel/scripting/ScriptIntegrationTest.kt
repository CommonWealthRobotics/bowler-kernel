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
import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDeviceResourceFactory
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.emptyImmutableList

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class ScriptIntegrationTest {

    @Test
    fun `provision LED integration test`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                val bowlerDeviceFactory = DeviceFactory(
                    hardwareRegistry,
                    DefaultResourceIdValidator()
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

                device.connect().unsafeRunSync()

                val unprovisionedDigitalOut = resourceFactory.makeUnprovisionedDigitalOut(
                    device,
                    DefaultAttachmentPoints.Pin(7)
                ).fold({ fail { "" } }, { it })

                device.add(unprovisionedDigitalOut).unsafeRunSync()

                device.disconnect().unsafeRunSync()

                return Either.right(null)
            }

            override fun stopScript() = Unit
        }

        script.startScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    @Test
    fun `register hardware inside a script`() {
        val script = object : Script() {
            private var worked = false

            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                val deviceFactory = DeviceFactory(
                    hardwareRegistry,
                    DefaultResourceIdValidator()
                )

                val resourceFactory = UnprovisionedDeviceResourceFactory(hardwareRegistry)

                deviceFactory.makeBowlerDevice(
                    DeviceId(
                        DefaultDeviceTypes.UnknownDevice,
                        DefaultConnectionMethods.RawHID(0, 0)
                    ),
                    mockBowlerRPCProtocol()
                ).map {
                    it.connect().unsafeRunSync()

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

                    it.add(led1).unsafeRunSync()
                    it.disconnect().unsafeRunSync()
                }
                return Either.right(worked)
            }

            @SuppressWarnings("EmptyFunctionBlock")
            override fun stopScript() {
            }
        }

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
        val script = object : Script() {
            private val counter = mutableListOf(1)

            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                return counter.right()
            }

            override fun stopScript() {
                counter.add(2)
            }
        }

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

    private fun failedToRunScript(it: String): Nothing =
        fail {
            """
            |Failed to run script:
            |$it
            """.trimMargin()
        }
}
