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
package com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid

import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

object UnknownDeviceType : DeviceType {

    override val name = "Unknown Device"

    /**
     * This method always returns true because we can't validate what we don't know anything about.
     * Rather than disallowing the user to use any device resources with this device, we would
     * rather skip this validation step. The Bowler protocol can still be used to validate the
     * resource.
     */
    override fun isResourceInRange(resourceId: ResourceId) = true
}
