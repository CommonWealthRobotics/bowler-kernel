/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

/**
 * An attachment point encoded as a [Byte], i.e. a pin number, etc.
 */
interface AttachmentPoint {

    val type: Byte

    /**
     * An extra byte for data about the attachment point.
     */
    val data: Byte
}
