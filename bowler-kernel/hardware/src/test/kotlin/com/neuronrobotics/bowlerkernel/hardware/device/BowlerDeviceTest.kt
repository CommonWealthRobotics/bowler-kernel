/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.device

import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.LED
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BowlerDeviceTest {

    private val device = BowlerDevice(SimpleDeviceId(""), mock())

    @Test
    fun `test provision resource with invalid resource type`() {
        val result = device.provisionResource {
            object : ProvisionedDeviceResource {
                override val device: Device
                    get() = TODO("not implemented")
                override val resourceId: ResourceId
                    get() = TODO("not implemented")
            }
        }

        assertTrue(result.isLeft())
    }

    @Test
    fun `test provision resource with LED`() {
        val result = device.provisionResource {
            mock<LED>()
        }

        assertTrue(result.isRight())
    }
}
