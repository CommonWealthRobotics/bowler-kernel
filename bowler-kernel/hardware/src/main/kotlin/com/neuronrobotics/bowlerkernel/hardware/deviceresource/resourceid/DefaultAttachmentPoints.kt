/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

import com.google.common.collect.ImmutableList
import org.octogonapus.guavautil.collections.emptyImmutableList
import org.octogonapus.guavautil.collections.immutableListOf

/**
 * The attachment points Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom attachment points.
 */
sealed class DefaultAttachmentPoints(
    override val type: Byte,
    override val data: ImmutableList<Byte> = emptyImmutableList()
) : AttachmentPoint {

    data class Pin(val pinNumber: Byte) : DefaultAttachmentPoints(1, immutableListOf(pinNumber))

    data class PinGroup(val pinNumbers: ImmutableList<Byte>) :
        DefaultAttachmentPoints(2, pinNumbers)

    data class USBPort(val portNumber: Byte) :
        DefaultAttachmentPoints(3, immutableListOf(portNumber))

    /**
     * The lowest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestTypeNumber(): Byte = 1

    /**
     * The highest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestTypeNumber(): Byte = 3
}
