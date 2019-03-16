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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource

/**
 * A [DeviceResource] which has been registered but not provisioned.
 */
abstract class UnprovisionedDeviceResource : DeviceResource {

    /**
     * Provisions this [DeviceResource] by communicating with the parent device to set up any
     * hardware-local requirements. For example, this could initialize GPIO pins or register
     * interrupt handlers.
     *
     * @return A [ProvisionedDeviceResource]
     */
    protected abstract fun provision(): ProvisionedDeviceResource

    /**
     * Runs and returns [provision].
     */
    internal fun runProvision() = provision()
}
