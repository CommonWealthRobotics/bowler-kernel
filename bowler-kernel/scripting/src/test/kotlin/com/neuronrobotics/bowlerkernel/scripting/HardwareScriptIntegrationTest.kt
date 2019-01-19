/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericServo
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOutFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedServoFactory
import com.nhaarman.mockitokotlin2.mock
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import org.octogonapus.guavautil.collections.emptyImmutableList
import javax.inject.Inject

class TestHardware
@Inject constructor(
    private val bowlerDeviceFactory: BowlerDeviceFactory,
    private val digitalOutFactoryFactory: UnprovisionedDigitalOutFactory.Factory,
    private val servoFactoryFactory: UnprovisionedServoFactory.Factory
) : Script() {

    override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
        val device = bowlerDeviceFactory.makeBowlerDevice(
            SimpleDeviceId("/dev/ttyACM0"),
            mock {}
        ).fold({ throw IllegalStateException(it) }, { it })

        val ledFactory = digitalOutFactoryFactory.create(device)
        ledFactory.makeUnprovisionedDigitalOut(
            DefaultAttachmentPoints.Pin(1)
        ).provisionOrFail() as GenericDigitalOut

        ledFactory.makeUnprovisionedDigitalOut(
            DefaultAttachmentPoints.Pin(2)
        ).provisionOrFail() as GenericDigitalOut

        val servoFactory = servoFactoryFactory.create(device)
        servoFactory.makeUnprovisionedServo(
            DefaultAttachmentPoints.Pin(3)
        ).provisionOrFail() as GenericServo

        return Either.right(null)
    }

    override fun stopScript() {
    }
}

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

        script.runScript(emptyImmutableList())
        script.stopAndCleanUp()
    }
}

private inline fun <reified A : UnprovisionedDeviceResource> Either<String, A>.provisionOrFail():
    ProvisionedDeviceResource {
    return fold({ throw IllegalStateException(it) }, { it }).provision()
        .fold({ throw IllegalStateException(it) }, { it })
}
