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
package com.neuronrobotics.bowlerkernel.hardware.device

import arrow.core.Option
import arrow.core.getOrElse
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericAnalogIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.GenericSerialConnection
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedAnalogIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedSerialConnection
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.fail

internal class BowlerDeviceTest {

    private val led1Id = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(1)
    )

    private val line1Id = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(2)
    )

    private val serial1Id = ResourceId(
        DefaultResourceTypes.SerialConnection,
        DefaultAttachmentPoints.USBPort(1)
    )

    private val unknownId = ResourceId(
        object : ResourceType {
            override val type = (DefaultResourceTypes.getHighestTypeNumber() + 1).toByte()
            override val sendLength = 0.toByte()
            override val receiveLength = 0.toByte()
        },
        DefaultAttachmentPoints.Pin(1)
    )

    private val bowlerRPCProtocol = mock<BowlerRPCProtocol> {
        on { addWrite(led1Id) } doReturn Option.empty()
        on { addRead(led1Id) } doReturn Option.just("")

        on { addWrite(line1Id) } doReturn Option.just("")
        on { addRead(line1Id) } doReturn Option.empty()

        on { addWrite(serial1Id) } doReturn Option.empty()
        on { addRead(serial1Id) } doReturn Option.empty()

        // Unknown resources can't be validated, so we must let them be added
        on { addWrite(unknownId) } doReturn Option.empty()
        on { addRead(unknownId) } doReturn Option.empty()
    }

    private val device = BowlerDevice(
        SimpleDeviceId(""),
        bowlerRPCProtocol,
        DefaultResourceIdValidator()
    )

    private val led1 = UnprovisionedDigitalOut(device, led1Id)

    private val line1 = UnprovisionedAnalogIn(device, line1Id)

    private val serial1 = UnprovisionedSerialConnection(device, serial1Id)

    private val unknownResource = UnprovisionedAnalogIn(device, unknownId)

    @Test
    fun `test adding a normal led`() {
        val result = device.add(led1)

        verify(bowlerRPCProtocol).addWrite(led1Id)
        verify(bowlerRPCProtocol, never()).addRead(led1Id)

        assertAll(
            { assertTrue(result.isRight()) },
            { assertTrue(result.getOrElse { fail { "" } } is GenericDigitalOut) }
        )
    }

    @Test
    fun `test adding a normal line sensor`() {
        val result = device.add(line1)

        verify(bowlerRPCProtocol, never()).addWrite(line1Id)
        verify(bowlerRPCProtocol).addRead(line1Id)

        assertAll(
            { assertTrue(result.isRight()) },
            { assertTrue(result.getOrElse { fail { "" } } is GenericAnalogIn) }
        )
    }

    @Test
    fun `test adding a serial connection`() {
        val result = device.add(serial1)

        verify(bowlerRPCProtocol).addWrite(serial1Id)
        verify(bowlerRPCProtocol).addRead(serial1Id)

        assertAll(
            { assertTrue(result.isRight()) },
            { assertTrue(result.getOrElse { fail { "" } } is GenericSerialConnection) }
        )
    }

    @Test
    fun `test adding an unknown resource`() {
        val result = device.add(unknownResource)

        verify(bowlerRPCProtocol, never()).addWrite(unknownId)
        verify(bowlerRPCProtocol, never()).addRead(unknownId)

        assertTrue(result.isLeft())
    }
}
