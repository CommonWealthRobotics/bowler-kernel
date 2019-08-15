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
package com.neuronrobotics.bowlerkernel.hardware.registry.error

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId

sealed class RegisterDeviceResourceGroupError : RegisterError {

    /**
     * Cannot register the resource group because its device is not registered.
     */
    data class DeviceIsNotRegisteredError(
        val resourceIds: ImmutableList<ResourceId>,
        val deviceId: DeviceId
    ) : RegisterDeviceResourceGroupError()

    /**
     * Cannot register the resource group because it is already registered.
     */
    data class ResourceGroupIsAlreadyRegisteredError(val resourceIds: ImmutableList<ResourceId>) :
        RegisterDeviceResourceGroupError()

    /**
     * Cannot register the resource group because there is already another resource on the same
     * attachment point.
     */
    data class ResourceOnSameAttachmentPointError(val resourceIds: ImmutableList<ResourceId>) :
        RegisterDeviceResourceGroupError()

    /**
     * Cannot register the resource group because it is outside the valid range of resources.
     */
    data class ResourceGroupMemberOutsideValidRangeError(val resourceIds: ImmutableList<ResourceId>) :
        RegisterDeviceResourceGroupError()
}
