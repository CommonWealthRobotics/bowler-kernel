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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDevice
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.PiezoelectricSpeaker
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.neuronrobotics.bowlerkernel.hardware.registry.error.RegisterError

interface UnprovisionedPiezoelectricSpeakerFactory {

    /**
     * Makes an unprovisioned [PiezoelectricSpeaker] attached to a device.
     *
     * @param device The device this piezoelectric speaker is attached to.
     * @param attachmentPoint The attachment point.
     * @return An unprovisioned [PiezoelectricSpeaker] on success, a [RegisterError] on failure.
     */
    fun makeUnprovisionedPiezoelectricSpeaker(
        device: BowlerDevice,
        attachmentPoint: AttachmentPoint
    ): Either<RegisterError, UnprovisionedDeviceResource<PiezoelectricSpeaker>>
}
