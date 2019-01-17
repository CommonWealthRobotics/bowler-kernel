/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints.PinGroup

/**
 * The resource types Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom resource types.
 */
sealed class DefaultResourceTypes(
    override val type: Byte
) : ResourceType {

    /**
     * A generic digital input.
     */
    object DigitalIn : DefaultResourceTypes(1)

    /**
     * A generic digital output.
     */
    object DigitalOut : DefaultResourceTypes(2)

    /**
     * A generic analog input.
     */
    object AnalogIn : DefaultResourceTypes(3)

    /**
     * A generic analog output.
     */
    object AnalogOut : DefaultResourceTypes(4)

    /**
     * A serial connection.
     */
    object SerialConnection : DefaultResourceTypes(5)

    /**
     * A servo.
     */
    object Servo : DefaultResourceTypes(6)

    /**
     * A stepper motor, typically needs a [PinGroup] of 2 or 4 pins.
     */
    object Stepper : DefaultResourceTypes(7)

    /**
     * A digital encoder.
     */
    object Encoder : DefaultResourceTypes(8)

    /**
     * A button (digital input with debouncing).
     */
    object Button : DefaultResourceTypes(9)

    /**
     * An ultrasonic sensor. Multiple ultrasonic sensors should be pinged round-robin.
     */
    object Ultrasonic : DefaultResourceTypes(10)

    /**
     * A piezoelectric speaker.
     */
    object PiezoelectricSpeaker : DefaultResourceTypes(11)

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
