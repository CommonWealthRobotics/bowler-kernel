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

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.GenericServoGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.util.ServoLimits

data class UnprovisionedServoGroup
internal constructor(
    override val device: BowlerDevice,
    override val resourceIds: ImmutableList<ResourceId>,
    val limits: ImmutableList<ServoLimits>
) : UnprovisionedDeviceResourceGroup<GenericServoGroup> {

    override fun provision() = GenericServoGroup(device, resourceIds, limits).also {
        it.write(limits.map { it.zero })
    }
}
