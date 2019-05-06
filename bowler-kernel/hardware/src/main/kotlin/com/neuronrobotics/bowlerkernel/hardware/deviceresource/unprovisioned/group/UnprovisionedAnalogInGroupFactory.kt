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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.AnalogInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.registry.RegisterError

interface UnprovisionedAnalogInGroupFactory {

    /**
     * Makes an unprovisioned [AnalogInGroup] attached to a device.
     *
     * @param device The device this [AnalogInGroup] is attached to.
     * @param attachmentPoints The attachment points.
     * @return An unprovisioned [AnalogInGroup] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedAnalogInGroup(
        device: BowlerDevice,
        attachmentPoints: ImmutableList<AttachmentPoint>
    ): Either<RegisterError, UnprovisionedDeviceResourceGroup<AnalogInGroup>>
}
