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
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedServoFactory
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableSetOf
import javax.inject.Inject

internal class HardwareScriptIntegrationTest {

    @Test
    fun `test registering some hardware`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                injector.getInstance(key<TestHardware>()).let {
                    it.runScript(args)
                    it.stopAndCleanUp()
                }
                return Either.right(null)
            }

            override fun stopScript() {
            }
        }

        script.addToInjector(script.getDefaultModules())
        script.runScript(emptyImmutableList())
        script.stopAndCleanUp()
    }

    private class TestHardware
    @Inject constructor(
        private val bowlerDeviceFactory: BowlerDeviceFactory,
        private val digitalOutFactoryFactory: UnprovisionedDigitalOutFactory.Factory,
        private val servoFactoryFactory: UnprovisionedServoFactory.Factory
    ) : Script() {

        override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
            val device = bowlerDeviceFactory.makeBowlerDevice(
                DeviceId(
                    DefaultDeviceTypes.UnknownDevice,
                    DefaultConnectionMethods.RawHID(0, 0)
                ),
                MockBowlerRPCProtocol()
            ).fold({ fail { "" } }, { it })

            device.connect()

            val ledFactory = digitalOutFactoryFactory.create(device)
            val servoFactory = servoFactoryFactory.create(device)

            val led1 = ledFactory.makeUnprovisionedDigitalOut(
                DefaultAttachmentPoints.Pin(1)
            ).fold({ fail { "" } }, { it })

            val led2 = ledFactory.makeUnprovisionedDigitalOut(
                DefaultAttachmentPoints.Pin(2)
            ).fold({ fail { "" } }, { it })

            device.add(immutableSetOf(led1, led2))

            val servo1 = servoFactory.makeUnprovisionedServo(
                DefaultAttachmentPoints.Pin(3)
            ).fold({ fail { "" } }, { it })

            device.add(servo1)

            device.disconnect()

            return Either.right(null)
        }

        override fun stopScript() {
        }
    }
}
