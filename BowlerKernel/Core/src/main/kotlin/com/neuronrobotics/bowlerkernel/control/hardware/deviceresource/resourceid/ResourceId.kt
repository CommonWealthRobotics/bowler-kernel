/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

/**
 * The id of a resource attached (physically) to a device.
 *
 * @param resourceType The type of this resource.
 * @param attachmentPoint Where this resource is attached.
 */
data class ResourceId(
    val resourceType: ResourceType,
    val attachmentPoint: AttachmentPoint
)
