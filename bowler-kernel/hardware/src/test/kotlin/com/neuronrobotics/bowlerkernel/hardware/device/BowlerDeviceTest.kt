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

import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedAnalogIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedSerialConnection
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableSet

internal class BowlerDeviceTest {

    private val led1Id = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(1)
    )

    private val led2Id = ResourceId(
        DefaultResourceTypes.DigitalOut,
        DefaultAttachmentPoints.Pin(2)
    )

    private val line1Id = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(3)
    )

    private val line2Id = ResourceId(
        DefaultResourceTypes.AnalogIn,
        DefaultAttachmentPoints.Pin(4)
    )

    private val serial1Id = ResourceId(
        DefaultResourceTypes.SerialConnection,
        DefaultAttachmentPoints.USBPort(1)
    )

    private val unknownResourceType = object : ResourceType {
        override val type = (DefaultResourceTypes.getHighestTypeNumber() + 1).toByte()
        override val sendLength = 0.toByte()
        override val receiveLength = 0.toByte()
    }

    private val unknownId1 = ResourceId(unknownResourceType, DefaultAttachmentPoints.Pin(1))
    private val unknownId2 = ResourceId(unknownResourceType, DefaultAttachmentPoints.Pin(2))

    private val bowlerRPCProtocol = mock<BowlerRPCProtocol> {
        on { addWrite(led1Id) } doReturn Unit.right()
        on { addRead(led1Id) } doReturn "".left()
        on { addPollingRead(led1Id) } doReturn "".left()

        on { addWriteGroup(immutableSetOf(led1Id, led2Id)) } doReturn Unit.right()
        on { addReadGroup(immutableSetOf(led1Id, led2Id)) } doReturn "".left()
        on { addPollingReadGroup(immutableSetOf(led1Id, led2Id)) } doReturn "".left()

        on { addWrite(line1Id) } doReturn "".left()
        on { addRead(line1Id) } doReturn Unit.right()
        on { addPollingRead(line1Id) } doReturn Unit.right()

        on { addWriteGroup(immutableSetOf(line1Id, line2Id)) } doReturn "".left()
        on { addReadGroup(immutableSetOf(line1Id, line2Id)) } doReturn Unit.right()
        on { addPollingReadGroup(immutableSetOf(line1Id, line2Id)) } doReturn Unit.right()

        on { addWrite(serial1Id) } doReturn Unit.right()
        on { addRead(serial1Id) } doReturn Unit.right()
        on { addPollingRead(serial1Id) } doReturn Unit.right()

        // Unknown resources can't be validated, so we must let them be added
        on { addWrite(unknownId1) } doReturn Unit.right()
        on { addRead(unknownId1) } doReturn Unit.right()
        on { addPollingRead(unknownId1) } doReturn Unit.right()
    }

    private val device = BowlerDevice(
        DeviceId(
            DefaultDeviceTypes.UnknownDevice,
            DefaultConnectionMethods.RawHID(0, 0)
        ),
        bowlerRPCProtocol,
        DefaultResourceIdValidator()
    )

    private val led1 = UnprovisionedDigitalOut(device, led1Id)
    private val led2 = UnprovisionedDigitalOut(device, led2Id)
    private val line1 = UnprovisionedAnalogIn(device, line1Id)
    private val line2 = UnprovisionedAnalogIn(device, line2Id)
    private val serial1 = UnprovisionedSerialConnection(device, serial1Id)
    private val unknownResource1 = UnprovisionedAnalogIn(device, unknownId1)
    private val unknownResource2 = UnprovisionedAnalogIn(device, unknownId2)

    @Test
    fun `test adding an led`() {
        val result = device.add(led1)

        verify(bowlerRPCProtocol).addWrite(led1Id)
        verify(bowlerRPCProtocol, never()).addRead(led1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a line sensor`() {
        val result = device.add(line1)

        verify(bowlerRPCProtocol, never()).addWrite(line1Id)
        verify(bowlerRPCProtocol).addRead(line1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a serial connection`() {
        val result = device.add(serial1)

        verify(bowlerRPCProtocol).addWrite(serial1Id)
        verify(bowlerRPCProtocol).addRead(serial1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding an unknown resource`() {
        val result = device.add(unknownResource1)

        verify(bowlerRPCProtocol, never()).addWrite(unknownId1)
        verify(bowlerRPCProtocol, never()).addRead(unknownId1)

        assertTrue(result.isLeft())
    }

    @Test
    fun `test adding an led group`() {
        val result = device.add(immutableSetOf(led1, led2))

        verify(bowlerRPCProtocol).addWriteGroup(immutableSetOf(led1Id, led2Id))
        verify(bowlerRPCProtocol, never()).addReadGroup(immutableSetOf(led1Id, led2Id))

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a line sensor group`() {
        val result = device.add(immutableSetOf(line1, line2))

        verify(bowlerRPCProtocol, never()).addWriteGroup(immutableSetOf(line1Id, line2Id))
        verify(bowlerRPCProtocol).addReadGroup(immutableSetOf(line1Id, line2Id))

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding an unknown resource group`() {
        val result = device.add(immutableSetOf(unknownResource1, unknownResource2))

        verify(bowlerRPCProtocol, never()).addWriteGroup(immutableSetOf(unknownId1, unknownId2))
        verify(bowlerRPCProtocol, never()).addReadGroup(immutableSetOf(unknownId1, unknownId2))

        assertTrue(result.isLeft())
    }

    @Nested
    inner class TestFailureToAddResource {

        private val failingProtocol = mock<BowlerRPCProtocol> {
            on { addRead(any()) } doReturn "".left()
            on { addWrite(any()) } doReturn "".left()
            on { addReadGroup(any()) } doReturn "".left()
            on { addWriteGroup(any()) } doReturn "".left()
        }

        private val write1Id = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(1)
        )

        private val write2Id = ResourceId(
            DefaultResourceTypes.DigitalOut,
            DefaultAttachmentPoints.Pin(2)
        )

        private val read1Id = ResourceId(
            DefaultResourceTypes.DigitalIn,
            DefaultAttachmentPoints.Pin(1)
        )

        private val read2Id = ResourceId(
            DefaultResourceTypes.DigitalIn,
            DefaultAttachmentPoints.Pin(2)
        )

        private val device = BowlerDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            ),
            failingProtocol,
            DefaultResourceIdValidator()
        )

        private val write1 = UnprovisionedDigitalOut(device, write1Id)
        private val write2 = UnprovisionedDigitalOut(device, write2Id)
        private val read1 = UnprovisionedDigitalIn(device, read1Id)
        private val read2 = UnprovisionedDigitalIn(device, read2Id)

        @Test
        fun `test failure to add read`() {
            val result = device.add(read1)

            verify(failingProtocol).addRead(read1Id)
            verify(failingProtocol, never()).addWrite(read1Id)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add write`() {
            val result = device.add(write1)

            verify(failingProtocol, never()).addRead(write1Id)
            verify(failingProtocol).addWrite(write1Id)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add read group`() {
            val resources = immutableSetOf(read1, read2)
            val ids = resources.map { it.resourceId }.toImmutableSet()
            val result = device.add(resources)

            verify(failingProtocol).addReadGroup(ids)
            verify(failingProtocol, never()).addWriteGroup(ids)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add write group`() {
            val resources = immutableSetOf(write1, write2)
            val ids = resources.map { it.resourceId }.toImmutableSet()
            val result = device.add(resources)

            verify(failingProtocol, never()).addReadGroup(ids)
            verify(failingProtocol).addWriteGroup(ids)

            assertTrue(result.isLeft())
        }
    }
}
