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

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints.PinGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes.Companion.getHighestTypeNumber
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes.Companion.getLowestTypeNumber

/**
 * The resource types Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom resource types.
 */
sealed class DefaultResourceTypes(
    override val type: Byte,
    override val sendLength: Byte,
    override val receiveLength: Byte
) : ResourceType {

    /**
     * A generic digital input.
     */
    object DigitalIn : DefaultResourceTypes(1, 0, 1)

    /**
     * A generic digital output.
     */
    object DigitalOut : DefaultResourceTypes(2, 1, 0)

    /**
     * A generic analog input.
     */
    object AnalogIn : DefaultResourceTypes(3, 0, 2)

    /**
     * A generic analog output.
     */
    object AnalogOut : DefaultResourceTypes(4, 2, 0)

    /**
     * A serial connection.
     */
    object SerialConnection : DefaultResourceTypes(5, 60, 60)

    /**
     * A servo.
     */
    object Servo : DefaultResourceTypes(6, 1, 0)

    /**
     * A stepper motor, typically needs a [PinGroup] of 2 or 4 pins.
     */
    object Stepper : DefaultResourceTypes(7, 4, 0)

    /**
     * A digital encoder.
     */
    object Encoder : DefaultResourceTypes(8, 0, 4)

    /**
     * A button (digital input with debouncing).
     */
    object Button : DefaultResourceTypes(9, 0, 1)

    /**
     * An ultrasonic sensor. Multiple ultrasonic sensors should be pinged round-robin.
     */
    object Ultrasonic : DefaultResourceTypes(10, 0, 4)

    /**
     * A piezoelectric speaker.
     */
    object PiezoelectricSpeaker : DefaultResourceTypes(11, 8, 0)

    companion object {

        /**
         * The lowest used type number.
         */
        @SuppressWarnings("FunctionOnlyReturningConstant")
        fun getLowestTypeNumber(): Byte = 1

        /**
         * The highest used type number.
         */
        @SuppressWarnings("FunctionOnlyReturningConstant")
        fun getHighestTypeNumber(): Byte = 11
    }
}
