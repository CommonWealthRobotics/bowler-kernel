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
import arrow.data.extensions.sequence.applicative.product
import arrow.data.fix
import arrow.data.k
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.util.isAllUnique
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.fail

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
        val disallowedPins = (type.firstPinNumber until type.numberOfPins)
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
            .map { it.toList() }
            .forEach { assertDisallowed(DefaultResourceTypes.SerialConnection, it) }

        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k())
            .fix()
            .map { it.toList() }
            .forEach { assertDisallowed(DefaultResourceTypes.Encoder, it) }

        assertDisallowedSteppers(disallowedPins)
    }

    private fun assertDisallowedSteppers(disallowedPins: List<Byte>) {
        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k())
            .fix()
            .map { it.toList() }
            .forEach { assertDisallowed(DefaultResourceTypes.Stepper, it) }

        ListK.applicative()
            .tupled(disallowedPins.k(), disallowedPins.k(), disallowedPins.k(), disallowedPins.k())
            .fix()
            .map { it.toList() }
            .forEach { assertDisallowed(DefaultResourceTypes.Stepper, it) }

        ListK.applicative()
            .tupled(
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k(),
                disallowedPins.k()
            )
            .fix()
            .map { it.toList() }
            .forEach { assertDisallowed(DefaultResourceTypes.Stepper, it) }
    }

    private fun assertSerial() {
        serialPins.forEach {
            assertAllowed(DefaultResourceTypes.SerialConnection, it.toList())
            assertAllowed(DefaultResourceTypes.SerialConnection, it.reverse().toList())
            assertDisallowed(DefaultResourceTypes.SerialConnection, Tuple2(it.a, it.a).toList())
            assertDisallowed(DefaultResourceTypes.SerialConnection, Tuple2(it.b, it.b).toList())
        }
    }

    private fun assertEncoder() {
        ListK.applicative()
            .tupled(digitalInPins.k(), digitalInPins.k())
            .fix()
            .map { it.toList() }
            .forEach {
                if (it.isAllUnique()) {
                    assertAllowed(DefaultResourceTypes.Encoder, it)
                } else {
                    assertDisallowed(DefaultResourceTypes.Encoder, it)
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
            .map { it.toList() }
            .forEach {
                if (it.isAllUnique()) {
                    assertAllowed(DefaultResourceTypes.Ultrasonic, it)
                    assertAllowed(DefaultResourceTypes.Ultrasonic, it.reversed())
                } else {
                    assertDisallowed(DefaultResourceTypes.Ultrasonic, it)
                }
            }
    }

    private fun assertStepper() {
        val edgeCasePins = digitalOutPins.firstFive() + digitalOutPins.lastFive()

        edgeCasePins.asSequence().k().let {
            it.product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .forEach { assertAllowed(DefaultResourceTypes.Stepper, it) }

        edgeCasePins.asSequence().k().let {
            it.product(it).product(it).product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .forEach { assertAllowed(DefaultResourceTypes.Stepper, it) }

        edgeCasePins.asSequence().k().let {
            it.product(it).product(it).product(it).product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .forEach { assertAllowed(DefaultResourceTypes.Stepper, it) }

        digitalOutPins.asSequence().k().let {
            it.product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .checkFirstNPins(DefaultResourceTypes.Stepper)

        digitalOutPins.asSequence().k().let {
            it.product(it).product(it).product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .checkFirstNPins(DefaultResourceTypes.Stepper)

        digitalOutPins.asSequence().k().let {
            it.product(it).product(it).product(it).product(it)
        }.map { it.toList() }
            .filter { it.isAllUnique() }
            .checkFirstNPins(DefaultResourceTypes.Stepper)
    }

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
            is IntRange -> it.map { it.toByte() }
            else -> fail { "Unknown pin input: $it" }
        }
    }.toSet().toList()

    private fun Sequence<List<Byte>>.checkFirstNPins(
        type: DefaultResourceTypes,
        count: Int = 1000
    ) {
        // Check the first count pins
        var index = 0
        for (elem in this) {
            if (index >= count) {
                return
            }

            assertAllowed(type, elem)

            index++
        }
    }

    private fun assertAllowed(resourceType: DefaultResourceTypes, pin: Byte) =
        assertAllowed(resourceType, DefaultAttachmentPoints.Pin(pin))

    private fun assertAllowed(resourceType: DefaultResourceTypes, pinGroup: List<Byte>) =
        assertAllowed(
            resourceType,
            DefaultAttachmentPoints.PinGroup(pinGroup.toByteArray())
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
        pinGroup: List<Byte>
    ) = assertDisallowed(
        resourceType,
        DefaultAttachmentPoints.PinGroup(pinGroup.toByteArray())
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

private fun <A> Tuple2<A, A>.toList(): List<A> = listOf(a, b)
private fun <A> Tuple4<A, A, A, A>.toList(): List<A> = listOf(a, b, c, d)
private fun <A> Tuple5<A, A, A, A, A>.toList(): List<A> = listOf(a, b, c, d, e)

private fun <E> List<E>.firstFive(): List<E> {
    val endIndex = if (size <= 4) size else 5
    return subList(0, endIndex)
}

private fun <E> List<E>.lastFive(): List<E> {
    val startIndex = if (size - 5 < 0) 0 else size - 5
    return subList(startIndex, size)
}

@DeviceTypeTestDsl
internal fun deviceTypeTest(
    type: DefaultDeviceTypes,
    configure: DeviceTypeScenario.() -> Unit
) {
    val scenario = DeviceTypeScenario(type)
    scenario.configure()
    scenario.runTest()
}
