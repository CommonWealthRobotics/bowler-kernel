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
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
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
        private val resourceFactory: UnprovisionedDigitalOutFactory
    ) : Script() {
        @Suppress("UNCHECKED_CAST")
        override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
            val device = deviceFactory.makeBowlerDevice(
                DeviceId(
                    DefaultDeviceTypes.Esp32wroom32,
                    DefaultConnectionMethods.InternetAddress(
                        InetAddress.getByAddress(
                            listOf(192, 168, 4, 1).map { it.toByte() }.toByteArray()
                        )
                    )
                )
            ).fold({ throw IllegalStateException(it) }, { it })

            val led1 = resourceFactory.makeUnprovisionedDigitalOut(
                device,
                DefaultAttachmentPoints.Pin(32)
            ).fold({ throw IllegalStateException(it) }, { it })

            val led2 = resourceFactory.makeUnprovisionedDigitalOut(
                device,
                DefaultAttachmentPoints.Pin(33)
            ).fold({ throw IllegalStateException(it) }, { it })

            device.connect().mapLeft { throw IllegalStateException(it) }

            device.add(immutableListOf(led1, led2)).fold(
                { throw IllegalStateException(it) },
                { it }
            )

            repeat(100) {
                if (it % 2 == 0) {
                    device.bowlerRPCProtocol.digitalWrite(
                        immutableListOf(
                            led1.resourceId to DigitalState.LOW,
                            led2.resourceId to DigitalState.HIGH
                        )
                    )
                } else {
                    device.bowlerRPCProtocol.digitalWrite(
                        immutableListOf(
                            led1.resourceId to DigitalState.HIGH,
                            led2.resourceId to DigitalState.LOW
                        )
                    )
                }

                Thread.sleep(500)
            }

            return Unit.right()
        }

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
