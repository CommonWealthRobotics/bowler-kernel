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
package com.neuronrobotics.bowlerkernel.hardware.device.deviceid

import arrow.core.Tuple2
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.util.isAllUnique

@DslMarker
annotation class DeviceTypeDsl

@DeviceTypeDsl
class DeviceTypeImplementation {

    private lateinit var digitalInPins: List<Byte>
    private lateinit var digitalOutPins: List<Byte>
    private lateinit var analogInPins: List<Byte>
    private lateinit var analogOutPins: List<Byte>
    private lateinit var serialPins: List<Tuple2<Byte, Byte>>

    private val singlePinResources = listOf(
        DefaultResourceTypes.DigitalIn,
        DefaultResourceTypes.DigitalOut,
        DefaultResourceTypes.AnalogIn,
        DefaultResourceTypes.AnalogOut,
        DefaultResourceTypes.Servo,
        DefaultResourceTypes.Encoder,
        DefaultResourceTypes.Button,
        DefaultResourceTypes.PiezoelectricSpeaker,
        DefaultResourceTypes.Ultrasonic // Only if the pin can be digital out AND in
    )

    private val multiPinResources = listOf(
        DefaultResourceTypes.SerialConnection,
        DefaultResourceTypes.Stepper,
        DefaultResourceTypes.Encoder,
        DefaultResourceTypes.Ultrasonic
    )

    fun isResourceInRange(resourceId: ResourceId): Boolean {
        return when (val p = resourceId.attachmentPoint) {
            is DefaultAttachmentPoints.Pin -> checkPin(resourceId.resourceType, p)

            is DefaultAttachmentPoints.PwmPin ->
                checkPin(resourceId.resourceType, DefaultAttachmentPoints.Pin(p.pinNumber))

            is DefaultAttachmentPoints.PinGroup -> checkPinGroup(resourceId.resourceType, p)

            else -> false
        }
    }

    private fun checkPin(
        type: ResourceType,
        pin: DefaultAttachmentPoints.Pin
    ): Boolean = when (type) {
        in singlePinResources -> when (type) {
            is DefaultResourceTypes.DigitalIn, DefaultResourceTypes.Encoder,
            DefaultResourceTypes.Button ->
                pin.pinNumber in digitalInPins

            is DefaultResourceTypes.DigitalOut, DefaultResourceTypes.PiezoelectricSpeaker ->
                pin.pinNumber in digitalOutPins

            is DefaultResourceTypes.AnalogIn -> pin.pinNumber in analogInPins

            is DefaultResourceTypes.AnalogOut, DefaultResourceTypes.Servo ->
                pin.pinNumber in analogOutPins

            is DefaultResourceTypes.Ultrasonic -> pin.pinNumber in digitalInPins &&
                pin.pinNumber in digitalOutPins

            else -> throw IllegalStateException("The implementation is missing a resource.")
        }

        else -> false
    }

    private fun checkPinGroup(
        type: ResourceType,
        pinGroup: DefaultAttachmentPoints.PinGroup
    ): Boolean = when (type) {
        in multiPinResources -> {
            if (pinGroup.pinNumbers.isAllUnique()) {
                when (type) {
                    is DefaultResourceTypes.SerialConnection -> pinGroup.twoPins() &&
                        pinGroup.pinNumbers.asTuple2().let {
                            it in serialPins || it.reverse() in serialPins
                        }

                    is DefaultResourceTypes.Encoder -> pinGroup.twoPins() &&
                        pinGroup.pinNumbers allIn digitalInPins

                    is DefaultResourceTypes.Ultrasonic -> pinGroup.twoPins() &&
                        pinGroup.pinNumbers.let {
                            it[0] in digitalInPins && it[1] in digitalOutPins ||
                                it[1] in digitalInPins && it[0] in digitalOutPins
                        }

                    is DefaultResourceTypes.Stepper -> stepperTwoPins(pinGroup) ||
                        stepperFourPins(pinGroup) || stepperFivePins(pinGroup)

                    else -> throw IllegalStateException("The implementation is missing a resource.")
                }
            } else {
                // Not all unique pins
                false
            }
        }

        else -> false
    }

    private fun stepperTwoPins(pinGroup: DefaultAttachmentPoints.PinGroup): Boolean =
        pinGroup.twoPins() && pinGroup.pinNumbers allIn digitalOutPins

    private fun stepperFourPins(pinGroup: DefaultAttachmentPoints.PinGroup): Boolean =
        pinGroup.fourPins() && pinGroup.pinNumbers allIn digitalOutPins

    private fun stepperFivePins(pinGroup: DefaultAttachmentPoints.PinGroup): Boolean =
        pinGroup.fivePins() && pinGroup.pinNumbers allIn digitalOutPins

    fun digitalIn(vararg pins: Any) {
        digitalInPins = pins.collectPins()
    }

    fun digitalOut(vararg pins: Any) {
        digitalOutPins = pins.collectPins()
    }

    fun analogIn(vararg pins: Any) {
        analogInPins = pins.collectPins()
    }

    fun analogOut(vararg pins: Any) {
        analogOutPins = pins.collectPins()
    }

    fun serial(vararg pins: Pair<Number, Number>) {
        serialPins = pins.map { Tuple2(it.first.toByte(), it.second.toByte()) }.toSet().toList()
    }

    private fun Array<out Any>.collectPins() = flatMap {
        when (it) {
            is Int -> listOf(it.toByte())
            is IntRange -> it.map(Int::toByte)
            else -> throw IllegalStateException("Unknown pin input: $it")
        }
    }.toSet().toList()
}

private infix fun ByteArray.allIn(list: Iterable<Byte>) = all { list.contains(it) }

private fun DefaultAttachmentPoints.PinGroup.twoPins() = pinNumbers.size == 2

private fun DefaultAttachmentPoints.PinGroup.fourPins() = pinNumbers.size == 4

private fun DefaultAttachmentPoints.PinGroup.fivePins() = pinNumbers.size == 5

private fun ByteArray.asTuple2(): Tuple2<Byte, Byte> {
    require(size == 2)
    return Tuple2(this[0], this[1])
}

@DeviceTypeDsl
fun implementDeviceType(
    configure: DeviceTypeImplementation.() -> Unit
): DeviceTypeImplementation {
    val scenario = DeviceTypeImplementation()
    scenario.configure()
    return scenario
}
