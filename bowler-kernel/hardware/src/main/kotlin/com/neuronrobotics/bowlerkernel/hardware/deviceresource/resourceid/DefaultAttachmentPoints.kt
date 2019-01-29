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
