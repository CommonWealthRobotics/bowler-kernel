/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
