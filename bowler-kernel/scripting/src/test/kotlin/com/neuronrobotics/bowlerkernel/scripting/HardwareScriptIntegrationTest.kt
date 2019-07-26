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
import arrow.core.extensions.either.monad.binding
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalOutGroupFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedServoFactory
import com.neuronrobotics.bowlerkernel.util.ServoLimits
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf
import javax.inject.Inject

internal class HardwareScriptIntegrationTest {

    @Test
    fun `test registering some hardware`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                injector.getInstance(key<TestHardware>()).let {
                    it.startScript(args)
                    it.stopAndCleanUp()
                }
                return Either.right(null)
            }

            override fun stopScript() {
            }
        }

        script.addToInjector(Script.getDefaultModules())
        script.startScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    private class TestHardware
    @Inject constructor(
        private val bowlerDeviceFactory: BowlerDeviceFactory,
        private val digitalOutGroupFactory: UnprovisionedDigitalOutGroupFactory,
        private val servoFactory: UnprovisionedServoFactory
    ) : Script() {

        override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
            return binding {
                val (device) = bowlerDeviceFactory.makeBowlerDevice(
                    DeviceId(
                        DefaultDeviceTypes.UnknownDevice,
                        DefaultConnectionMethods.RawHID(0, 0)
                    ),
                    mockBowlerRPCProtocol()
                )

                device.connect().bind()

                val (ledGroup) = digitalOutGroupFactory.makeUnprovisionedDigitalOutGroup(
                    device,
                    immutableListOf(
                        DefaultAttachmentPoints.Pin(1),
                        DefaultAttachmentPoints.Pin(2)
                    )
                )

                device.add(ledGroup).bind()

                val (servo1) = servoFactory.makeUnprovisionedServo(
                    device,
                    DefaultAttachmentPoints.Pin(3),
                    ServoLimits(100, 0, 50, 1)
                )

                device.add(servo1).bind()

                device.disconnect().bind()
                null
            }
        }

        override fun stopScript() {
        }
    }
}
