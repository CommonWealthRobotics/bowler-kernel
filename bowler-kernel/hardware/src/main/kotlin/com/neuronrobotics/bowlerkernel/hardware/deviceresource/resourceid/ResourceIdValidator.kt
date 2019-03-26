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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

import arrow.core.Either

/**
 * Validates various properties of a [ResourceId].
 */
interface ResourceIdValidator {

    /**
     * Validates the attachment point is on the device.
     *
     * @param attachmentPoint The attachment point.
     * @return An error.
     */
    fun valdiateAttachmentPoint(attachmentPoint: AttachmentPoint): Either<String, Unit>

    /**
     * Validates the resource can be read from.
     *
     * @param resourceType The resource.
     * @return An error.
     */
    fun validateIsReadType(resourceType: ResourceType): Either<String, Unit>

    /**
     * Validates the resource can be written to.
     *
     * @param resourceType The resource.
     * @return An error.
     */
    fun validateIsWriteType(resourceType: ResourceType): Either<String, Unit>
}
