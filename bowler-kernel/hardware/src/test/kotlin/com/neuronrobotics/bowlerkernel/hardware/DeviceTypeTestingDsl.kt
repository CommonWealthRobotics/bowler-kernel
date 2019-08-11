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
package com.neuronrobotics.bowlerkernel.hardware

import arrow.core.Tuple2
import arrow.data.ListK
import arrow.data.extensions.listk.applicative.applicative
import arrow.data.fix
import arrow.data.k
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@DslMarker
internal annotation class DeviceTypeTestDsl

@DeviceTypeTestDsl
internal class DeviceTypeScenario(
    private val type: DefaultDeviceTypes
) {

    private lateinit var digitalInPins: List<Byte>
    private lateinit var digitalOutPins: List<Byte>
    private lateinit var analogInPins: List<Byte>
    private lateinit var analogOutPins: List<Byte>
    private lateinit var serialPins: List<Pair<Byte, Byte>>
    private lateinit var disallowedPins: List<Byte>

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
        }

        digitalOutPins.forEach {
            assertAllowed(DefaultResourceTypes.DigitalOut, it)
        }

        analogInPins.forEach {
            assertAllowed(DefaultResourceTypes.AnalogIn, it)
        }

        analogOutPins.forEach {
            assertAllowed(DefaultResourceTypes.AnalogOut, it)
        }

        serialPins.forEach {
            assertAllowed(DefaultResourceTypes.SerialConnection, it)
        }

        ListK.applicative()
            .tupled(disallowedPins.k(), singlePinResources.k())
            .fix()
            .forEach { (pin, resourceType) ->
                assertDisallowed(resourceType, pin)
            }

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

    fun digitalIn(vararg pins: Byte) {
        digitalInPins = pins.toList()
    }

    fun digitalOut(vararg pins: Byte) {
        digitalOutPins = pins.toList()
    }

    fun analogIn(vararg pins: Byte) {
        analogInPins = pins.toList()
    }

    fun analogOut(vararg pins: Byte) {
        analogOutPins = pins.toList()
    }

    fun serial(vararg pins: Pair<Number, Number>) {
        serialPins = pins.map { it.first.toByte() to it.second.toByte() }
    }

    fun disallowed(vararg pins: Byte) {
        disallowedPins = pins.toList()
    }

    private fun assertAllowed(resourceType: DefaultResourceTypes, pin: Byte) =
        assertAllowed(resourceType, DefaultAttachmentPoints.Pin(pin))

    private fun assertAllowed(resourceType: DefaultResourceTypes, pinGroup: Pair<Byte, Byte>) =
        assertAllowed(
            resourceType,
            DefaultAttachmentPoints.PinGroup(byteArrayOf(pinGroup.first, pinGroup.second))
        )

    private fun assertAllowed(resourceType: DefaultResourceTypes, pinGroup: Tuple2<Byte, Byte>) =
        assertAllowed(
            resourceType,
            DefaultAttachmentPoints.PinGroup(byteArrayOf(pinGroup.a, pinGroup.b))
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
        attachmentPoint: DefaultAttachmentPoints
    ) {
        assertFalse(
            type.isResourceInRange(ResourceId(resourceType, attachmentPoint)),
            "Resource $resourceType on pin $attachmentPoint should be disallowed."
        )
    }
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
