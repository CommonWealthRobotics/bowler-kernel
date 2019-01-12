/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid

/**
 * The resource types Bowler supports out-of-the-box. Uses a continuous range of bytes from
 * [getLowestTypeNumber] through [getHighestTypeNumber]. Any numbers outside that range are
 * available for adding custom resource types.
 */
sealed class DefaultResourceTypes(
    override val type: Byte
) : ResourceType {

    object DigitalIn : DefaultResourceTypes(1)
    object DigitalOut : DefaultResourceTypes(2)
    object AnalogIn : DefaultResourceTypes(3)
    object AnalogOut : DefaultResourceTypes(4)
    object Serial : DefaultResourceTypes(5)
    object RGBLed : DefaultResourceTypes(6)
    object Servo : DefaultResourceTypes(7)
    object Stepper : DefaultResourceTypes(8)
    object DCMotor : DefaultResourceTypes(9)
    object BLDCMotor : DefaultResourceTypes(10)
    object Potentiometer : DefaultResourceTypes(11)
    object Encoder : DefaultResourceTypes(12)
    object QuadratureEncoder : DefaultResourceTypes(13)
    object Button : DefaultResourceTypes(14)
    object Infrared : DefaultResourceTypes(15)
    object Ultrasonic : DefaultResourceTypes(16)
    object PiezoelectricSpeaker : DefaultResourceTypes(17)

    /**
     * The lowest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getLowestTypeNumber(): Byte = 1

    /**
     * The highest used type number.
     */
    @SuppressWarnings("FunctionOnlyReturningConstant")
    fun getHighestTypeNumber(): Byte = 17
}
