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
@file:SuppressWarnings("LongMethod")

package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.extensions.either.monad.binding
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalOutGroupFactory
import org.jlleitschuh.guice.getInstance
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import java.net.InetAddress
import javax.inject.Inject

internal class TestWithEsp32 {

    private class TestScript
    @Inject constructor(
        private val deviceFactory: BowlerDeviceFactory,
        private val digitalOutGroupFactory: UnprovisionedDigitalOutGroupFactory
    ) : Script() {
        @Suppress("UNCHECKED_CAST")
        override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
            return binding {
                val (device) = deviceFactory.makeBowlerDevice(
                    DeviceId(
                        DefaultDeviceTypes.Esp32wroom32,
                        DefaultConnectionMethods.InternetAddress(
                            InetAddress.getByAddress(
                                listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                            )
                        )
                    )
                )

                val (unprovisionedLedGroup) =
                    digitalOutGroupFactory.makeUnprovisionedDigitalOutGroup(
                        device,
                        immutableListOf(
                            DefaultAttachmentPoints.Pin(32),
                            DefaultAttachmentPoints.Pin(33)
                        )
                    )

                device.connect().bind()

                val (ledGroup) = device.add(unprovisionedLedGroup)

                repeat(100) {
                    if (it % 2 == 0) {
                        ledGroup.write(immutableListOf(DigitalState.LOW, DigitalState.HIGH))
                    } else {
                        ledGroup.write(immutableListOf(DigitalState.HIGH, DigitalState.LOW))
                    }

                    Thread.sleep(500)
                }

                Unit
            }
        }

        @SuppressWarnings("EmptyFunctionBlock")
        override fun stopScript() {
        }
    }

    @Test
    @Disabled
    fun `test esp32 with script`() {
        val script = Script.makeScriptInjector().createChildInjector(
            Script.getDefaultModules()
        ).getInstance<TestScript>()

        val result = script.startScript(emptyImmutableList())
        println(result)
    }
}
