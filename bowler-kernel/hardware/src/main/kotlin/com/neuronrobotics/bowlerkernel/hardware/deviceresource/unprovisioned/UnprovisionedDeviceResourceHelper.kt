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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned

import arrow.core.Either
import arrow.core.left
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.CountDownLatch

internal fun <T : ProvisionedDeviceResource> provisionBlocking(
    device: BowlerDevice,
    resourceId: ResourceId,
    makeProvisionedDeviceResource: () -> T
): Either<ProvisionError, T> {
    val latch = CountDownLatch(1)
    var out: Either<ProvisionError, T> = "Return value not initialized.".left()

//    device.bowlerRPCProtocol.provisionResource(
//        resourceId,
//        {
//            out = "Provision command timed out.".left()
//            latch.countDown()
//        },
//        {
//            out = makeProvisionedDeviceResource().right()
//            latch.countDown()
//        }
//    )
    TODO("Not implemented")

//    return out
}
