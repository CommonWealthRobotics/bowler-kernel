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
import arrow.core.Tuple4
import arrow.core.Tuple5
import arrow.data.ListK
import arrow.data.extensions.listk.applicative.applicative
import arrow.data.fix
import arrow.data.k
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.util.isAllUnique
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@DslMarker
internal annotation class DeviceTypeTestDsl

@SuppressWarnings("TooManyFunctions", "LargeClass", "LongMethod")
@DeviceTypeTestDsl
internal class DeviceTypeScenario(
    private val type: DefaultDeviceTypes
) {

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
        DefaultResourceTypes.PiezoelectricSpeaker
    )

    fun runTest() {
        digitalInPins.forEach {
            assertAllowed(DefaultResourceTypes.DigitalIn, it)
            assertAllowed(DefaultResourceTypes.Encoder, it)
            assertAllowed(DefaultResourceTypes.Button, it)
        }

        digitalOutPins.forEach {
            assertAllowed(DefaultResourceTypes.DigitalOut, it)
            assertAllowed(DefaultResourceTypes.PiezoelectricSpeaker, it)
        }

        analogInPins.forEach {
            assertAllowed(DefaultResourceTypes.AnalogIn, it)
        }

        analogOutPins.forEach {
            assertAllowed(DefaultResourceTypes.AnalogOut, it)
            assertAllowed(DefaultResourceTypes.Servo, it)
        }

        assertSerial()
        assertEncoder()
        assertUltrasonic()
        assertStepper()
        assertDisallowedPins()
    }

    private fun assertDisallowedPins() {
        val disallowedPins = (type.firstPinNumber until type.numberOfPins).toList()
            .map { it.toByte() } - digitalInPins - digitalOutPins - analogInPins - analogOutPins -
            serialPins.flatMap { listOf(it.a, it.b) }

        ListK.applicative()
            .tupled(singlePinResources.k(), disallowedPins.k())
            .fix()
            .forEach { (resourceType, pin) ->
                assertDisallowed(resourceType, pin)
            }

        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k())
            .fix()
            .forEach {
                assertDisallowed(DefaultResourceTypes.SerialConnection, it)
            }

        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k())
            .fix()
            .forEach {
                assertDisallowed(DefaultResourceTypes.Encoder, it)
            }

        assertDisallowedSteppers(disallowedPins)
    }

    private fun assertDisallowedSteppers(disallowedPins: List<Byte>) {
        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k())
            .fix()
            .forEach {
                assertDisallowed(DefaultResourceTypes.Stepper, it)
            }

        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k(), disallowedPins.k(), disallowedPins.k())
            .fix()
            .forEach {
                assertDisallowed(DefaultResourceTypes.Stepper, it)
            }

        ListK.applicative()
            .tupled(
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k()
            )
            .fix()
            .forEach {
                assertDisallowed(DefaultResourceTypes.Stepper, it)
            }
    }

    private fun assertSerial() {
        serialPins.forEach {
            assertAllowed(DefaultResourceTypes.SerialConnection, it)
            assertAllowed(DefaultResourceTypes.SerialConnection, it.reverse())
            assertDisallowed(DefaultResourceTypes.SerialConnection, Tuple2(it.a, it.a))
            assertDisallowed(DefaultResourceTypes.SerialConnection, Tuple2(it.b, it.b))
        }
    }

    private fun assertEncoder() {
        ListK.applicative()
            .tupled(digitalInPins.k(), digitalInPins.k())
            .fix()
            .forEach {
                if (it.a == it.b) {
                    assertDisallowed(DefaultResourceTypes.Encoder, it)
                } else {
                    assertAllowed(DefaultResourceTypes.Encoder, it)
                }
            }
    }

    private fun assertUltrasonic() {
        // Can have an ultrasonic on one pin if it can be digital in AND out
        digitalInPins.union(digitalOutPins).forEach {
            assertAllowed(DefaultResourceTypes.Ultrasonic, it)
        }

        ListK.applicative()
            .tupled(digitalInPins.k(), digitalOutPins.k())
            .fix()
            .forEach {
                if (it.a == it.b) {
                    assertDisallowed(DefaultResourceTypes.Ultrasonic, it)
                } else {
                    assertAllowed(DefaultResourceTypes.Ultrasonic, it)
                    assertAllowed(DefaultResourceTypes.Ultrasonic, it.reverse())
                }
            }
    }

    private fun assertStepper() {
        ListK.applicative()
            .tupled(digitalOutPins.k(), digitalOutPins.k())
            .fix()
            .filter { it.a != it.b }
            .forEach {
                assertAllowed(DefaultResourceTypes.Stepper, it)
            }

        ListK.applicative()
            .tupled(digitalOutPins.k(), digitalOutPins.k(), digitalOutPins.k(), digitalOutPins.k())
            .fix()
            .filter { it.allUnique() }
            .forEach {
                assertAllowed(DefaultResourceTypes.Stepper, it)
            }

        ListK.applicative()
            .tupled(
                digitalOutPins.k(),
                digitalOutPins.k(),
                digitalOutPins.k(),
                digitalOutPins.k(),
                digitalOutPins.k()
            )
            .fix()
            .filter { it.allUnique() }
            .forEach {
                assertAllowed(DefaultResourceTypes.Stepper, it)
            }
    }

    fun digitalIn(vararg pins: Byte) {
        digitalInPins = pins.toSet().toList()
    }

    fun digitalOut(vararg pins: Byte) {
        digitalOutPins = pins.toSet().toList()
    }

    fun analogIn(vararg pins: Byte) {
        analogInPins = pins.toSet().toList()
    }

    fun analogOut(vararg pins: Byte) {
        analogOutPins = pins.toSet().toList()
    }

    fun serial(vararg pins: Pair<Number, Number>) {
        serialPins = pins.map { Tuple2(it.first.toByte(), it.second.toByte()) }.toSet().toList()
    }

    private fun assertAllowed(resourceType: DefaultResourceTypes, pin: Byte) =
        assertAllowed(resourceType, DefaultAttachmentPoints.Pin(pin))

    private fun assertAllowed(resourceType: DefaultResourceTypes, pinGroup: Tuple2<Byte, Byte>) =
        assertAllowed(
            resourceType,
            DefaultAttachmentPoints.PinGroup(byteArrayOf(pinGroup.a, pinGroup.b))
        )

    private fun assertAllowed(
        resourceType: DefaultResourceTypes,
        pinGroup: Tuple4<Byte, Byte, Byte, Byte>
    ) = assertAllowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(
            byteArrayOf(
                pinGroup.a,
                pinGroup.b,
                pinGroup.c,
                pinGroup.d
            )
        )
    )

    private fun assertAllowed(
        resourceType: DefaultResourceTypes,
        pinGroup: Tuple5<Byte, Byte, Byte, Byte, Byte>
    ) = assertAllowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(
            byteArrayOf(
                pinGroup.a,
                pinGroup.b,
                pinGroup.c,
                pinGroup.d,
                pinGroup.e
            )
        )
    )

    private fun assertAllowed(
        resourceType: DefaultResourceTypes,
        attachmentPoint: DefaultAttachmentPoints
    ) {
        assertTrue(
            type.isResourceInRange(ResourceId(resourceType, attachmentPoint)),
            "Resource $resourceType on pin $attachmentPoint should be allowed."
        )
    }

    private fun assertDisallowed(
        resourceType: DefaultResourceTypes,
        pin: Byte
    ) = assertDisallowed(resourceType, DefaultAttachmentPoints.Pin(pin))

    private fun assertDisallowed(
        resourceType: DefaultResourceTypes,
        pinGroup: Tuple2<Byte, Byte>
    ) = assertDisallowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(byteArrayOf(pinGroup.a, pinGroup.b))
    )

    private fun assertDisallowed(
        resourceType: DefaultResourceTypes,
        pinGroup: Tuple4<Byte, Byte, Byte, Byte>
    ) = assertDisallowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(
            byteArrayOf(
                pinGroup.a,
                pinGroup.b,
                pinGroup.c,
                pinGroup.d
            )
        )
    )

    private fun assertDisallowed(
        resourceType: DefaultResourceTypes,
        pinGroup: Tuple5<Byte, Byte, Byte, Byte, Byte>
    ) = assertDisallowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(
            byteArrayOf(
                pinGroup.a,
                pinGroup.b,
                pinGroup.c,
                pinGroup.d,
                pinGroup.e
            )
        )
    )

    private fun assertDisallowed(
        resourceType: DefaultResourceTypes,
        attachmentPoint: DefaultAttachmentPoints
    ) {
        assertFalse(
            type.isResourceInRange(ResourceId(resourceType, attachmentPoint)),
            "Resource $resourceType on pin $attachmentPoint should be disallowed."
        )
    }
}

private fun <A, B, C, D> Tuple4<A, B, C, D>.allUnique() = listOf(a, b, c, d).isAllUnique()
private fun <A, B, C, D, E> Tuple5<A, B, C, D, E>.allUnique() = listOf(a, b, c, d, e).isAllUnique()

@DeviceTypeTestDsl
internal fun deviceTypeTest(
    type: DefaultDeviceTypes,
    configure: DeviceTypeScenario.() -> Unit
) {
    val scenario = DeviceTypeScenario(type)
    scenario.configure()
    scenario.runTest()
}
