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

/**
 * The id of a resource attached (physically) to a device.
 *
 * @param resourceType The type of this resource.
 * @param attachmentPoint Where this resource is attached.
 */
data class ResourceId(
    val resourceType: ResourceType,
    val attachmentPoint: AttachmentPoint
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceId

        if (attachmentPoint != other.attachmentPoint) return false

        return true
    }

    override fun hashCode() = attachmentPoint.hashCode()
}
