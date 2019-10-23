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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup

import arrow.effects.IO

interface PiezoelectricSpeaker :
    ProvisionedDeviceResource {

    /**
     * Plays a tone of [frequency] Hz.
     *
     * @param frequency The frequency of the tone in Hz.
     */
    fun playTone(frequency: Long): IO<Unit>

    /**
     * Plays a tone of [frequency] Hz for [duration] ms.
     *
     * @param frequency The frequency of the tone in Hz.
     * @param duration The duration of the tone in ms.
     */
    fun playTone(frequency: Long, duration: Long): IO<Unit>
}
