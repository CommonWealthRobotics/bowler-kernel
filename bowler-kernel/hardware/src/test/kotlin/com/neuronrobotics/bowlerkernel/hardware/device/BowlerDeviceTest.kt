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

import arrow.core.Either
import arrow.core.Right
import arrow.core.right
import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultDeviceTypes
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.group.ProvisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceType
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedAnalogInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDeviceResourceGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalInGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.group.UnprovisionedDigitalOutGroup
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedAnalogIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDigitalIn
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedDigitalOut
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.nongroup.UnprovisionedSerialConnection
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet

@SuppressWarnings("TooManyFunctions")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
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

    private val testResourceId1 = ResourceId(
        DefaultResourceTypes.Encoder,
        DefaultAttachmentPoints.PinGroup(byteArrayOf(1, 2))
    )

    private val unknownResourceType = object : ResourceType {
        override val type = (DefaultResourceTypes.getHighestTypeNumber() + 1).toByte()
        override val sendLength = 0.toByte()
        override val receiveLength = 0.toByte()
    }

    private val unknownId1 = ResourceId(unknownResourceType, DefaultAttachmentPoints.Pin(1))
    private val unknownId2 = ResourceId(unknownResourceType, DefaultAttachmentPoints.Pin(2))

    private val bowlerRPCProtocol = mock<BowlerRPCProtocol> {
        on { connect() } doReturn IO.just(Unit)
        on { disconnect() } doReturn IO.just(Unit)
        on { isResourceInRange(testResourceId1) } doReturn IO.just(true)

        on { addWrite(led1Id) } doReturn IO.just(Unit)
        on { addRead(led1Id) } doReturn IO.raiseError(UnsupportedOperationException(""))
        on { addWriteRead(led1Id) } doReturn IO.raiseError(UnsupportedOperationException(""))

        on { addWriteGroup(immutableSetOf(led1Id, led2Id)) } doReturn IO.just(Unit)
        on { addReadGroup(immutableSetOf(led1Id, led2Id)) } doReturn IO.raiseError(
            UnsupportedOperationException("")
        )

        on { addWrite(line1Id) } doReturn IO.raiseError(UnsupportedOperationException(""))
        on { addRead(line1Id) } doReturn IO.just(Unit)
        on { addWriteRead(line1Id) } doReturn IO.raiseError(UnsupportedOperationException(""))

        on { addWriteGroup(immutableSetOf(line1Id, line2Id)) } doReturn IO.raiseError(
            UnsupportedOperationException("")
        )
        on { addReadGroup(immutableSetOf(line1Id, line2Id)) } doReturn IO.just(Unit)
        on { addWriteReadGroup(immutableSetOf(line1Id, line2Id)) } doReturn IO.raiseError(
            UnsupportedOperationException("")
        )

        on { addWrite(serial1Id) } doReturn IO.just(Unit)
        on { addRead(serial1Id) } doReturn IO.just(Unit)
        on { addWriteRead(serial1Id) } doReturn IO.just(Unit)

        // Unknown resources can't be validated, so we must let them be added
        on { addWrite(unknownId1) } doReturn IO.just(Unit)
        on { addRead(unknownId1) } doReturn IO.just(Unit)
        on { addWriteRead(unknownId1) } doReturn IO.just(Unit)
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
    private val ledGroup = UnprovisionedDigitalOutGroup(device, immutableListOf(led1Id, led2Id))
    private val line1 = UnprovisionedAnalogIn(device, line1Id)
    private val lineGroup = UnprovisionedAnalogInGroup(device, immutableListOf(line1Id, line2Id))
    private val serial1 = UnprovisionedSerialConnection(device, serial1Id)
    private val unknownResource1 = UnprovisionedAnalogIn(device, unknownId1)
    private val unknownResource2 = UnprovisionedAnalogIn(device, unknownId2)
    private val unknownGroup =
        UnprovisionedAnalogInGroup(device, immutableListOf(unknownId1, unknownId2))

    @Test
    fun `test connect`() {
        val result = device.connect().attempt().unsafeRunSync()

        assertEquals(Right(Unit), result)

        verify(bowlerRPCProtocol).connect()
    }

    @Test
    fun `test disconnect`() {
        val result = device.disconnect().attempt().unsafeRunSync()

        assertEquals(Right(Unit), result)

        verify(bowlerRPCProtocol).disconnect()
    }

    @Test
    fun `test isResourceInRange`() {
        val result = device.isResourceInRange(testResourceId1).attempt().unsafeRunSync()

        assertEquals(Right(true), result)

        verify(bowlerRPCProtocol).isResourceInRange(testResourceId1)
    }

    @Test
    fun `test adding an led`() {
        val result = device.add(led1).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol).addWrite(led1Id)
        verify(bowlerRPCProtocol, never()).addRead(led1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a line sensor`() {
        val result = device.add(line1).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol, never()).addWrite(line1Id)
        verify(bowlerRPCProtocol).addRead(line1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a serial connection`() {
        val result = device.add(serial1).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol).addWriteRead(serial1Id)
        verify(bowlerRPCProtocol, never()).addWrite(serial1Id)
        verify(bowlerRPCProtocol, never()).addRead(serial1Id)

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding an unknown resource`() {
        val result = device.add(unknownResource1).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol, never()).addWrite(unknownId1)
        verify(bowlerRPCProtocol, never()).addRead(unknownId1)

        assertTrue(result.isLeft())
    }

    @Test
    fun `test adding an led group`() {
        val result = device.add(ledGroup).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol).addWriteGroup(immutableSetOf(led1Id, led2Id))
        verify(bowlerRPCProtocol, never()).addReadGroup(immutableSetOf(led1Id, led2Id))

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding a line sensor group`() {
        val result = device.add(lineGroup).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol, never()).addWriteGroup(immutableSetOf(line1Id, line2Id))
        verify(bowlerRPCProtocol).addReadGroup(immutableSetOf(line1Id, line2Id))

        assertTrue(result.isRight())
    }

    @Test
    fun `test adding an unknown resource group`() {
        val result = device.add(unknownGroup).attempt().unsafeRunSync()

        verify(bowlerRPCProtocol, never()).addWriteGroup(immutableSetOf(unknownId1, unknownId2))
        verify(bowlerRPCProtocol, never()).addReadGroup(immutableSetOf(unknownId1, unknownId2))

        assertTrue(result.isLeft())
    }

    @Test
    fun `test add group with non-unique ids`() {
        val result = device.add(
            UnprovisionedDigitalOutGroup(
                device,
                immutableListOf(led1Id, led2Id, led1Id)
            )
        ).attempt().unsafeRunSync()

        assertTrue(result is Either.Left)

        verify(bowlerRPCProtocol, never()).addRead(any(), any())
        verify(bowlerRPCProtocol, never()).addReadGroup(any(), any())
    }

    @Test
    fun `test adding writeRead group`() {
        val resourceType = mock<ResourceType> {
            on { type } doReturn 1
            on { sendLength } doReturn 1
            on { receiveLength } doReturn 1
        }

        val resourceIds = immutableSetOf(ResourceId(resourceType, DefaultAttachmentPoints.None))
        val mockProvisionedGroup = mock<ProvisionedDeviceResourceGroup> {
            on { this.resourceIds } doReturn resourceIds.toImmutableList()
        }
        val resources = mock<UnprovisionedDeviceResourceGroup<ProvisionedDeviceResourceGroup>> {
            on { provision() } doReturn mockProvisionedGroup
            on { this.resourceIds } doReturn resourceIds.toImmutableList()
        }

        val rpc = mock<BowlerRPCProtocol> {
            on { addReadGroup(resourceIds) } doReturn IO.just(Unit)
            on { addWriteGroup(resourceIds) } doReturn IO.just(Unit)
            on { addWriteReadGroup(resourceIds) } doReturn IO.just(Unit)
        }

        val device = BowlerDevice(
            DeviceId(
                DefaultDeviceTypes.UnknownDevice,
                DefaultConnectionMethods.RawHID(0, 0)
            ),
            rpc,
            mock {
                on { validateIsReadType(resourceType) } doReturn Unit.right()
                on { validateIsWriteType(resourceType) } doReturn Unit.right()
            }
        )

        val result = device.add(resources).attempt().unsafeRunSync()

        verify(rpc).addWriteReadGroup(resourceIds)
        verify(rpc, never()).addWriteGroup(resourceIds)
        verify(rpc, never()).addReadGroup(resourceIds)

        assertTrue(result.isRight())
    }

    @Nested
    inner class TestFailureToAddResource {

        private val failingProtocol = mock<BowlerRPCProtocol> {
            on { addRead(any(), any()) } doReturn IO.raiseError(UnsupportedOperationException(""))
            on { addWrite(any(), any()) } doReturn IO.raiseError(UnsupportedOperationException(""))
            on {
                addReadGroup(
                    any(),
                    any()
                )
            } doReturn IO.raiseError(UnsupportedOperationException(""))
            on { addWriteGroup(any(), any()) } doReturn IO.raiseError(
                UnsupportedOperationException(
                    ""
                )
            )
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
        private val writeGroup =
            UnprovisionedDigitalOutGroup(device, immutableListOf(write1Id, write2Id))
        private val read1 = UnprovisionedDigitalIn(device, read1Id)
        private val readGroup =
            UnprovisionedDigitalInGroup(device, immutableListOf(read1Id, read2Id))

        @Test
        fun `test failure to add read`() {
            val result = device.add(read1).attempt().unsafeRunSync()

            verify(failingProtocol).addRead(read1Id)
            verify(failingProtocol, never()).addWrite(read1Id)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add write`() {
            val result = device.add(write1).attempt().unsafeRunSync()

            verify(failingProtocol, never()).addRead(write1Id)
            verify(failingProtocol).addWrite(write1Id)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add read group`() {
            val ids = readGroup.resourceIds.toImmutableSet()
            val result = device.add(readGroup).attempt().unsafeRunSync()

            verify(failingProtocol).addReadGroup(ids)
            verify(failingProtocol, never()).addWriteGroup(ids)

            assertTrue(result.isLeft())
        }

        @Test
        fun `test failure to add write group`() {
            val ids = writeGroup.resourceIds.toImmutableSet()
            val result = device.add(writeGroup).attempt().unsafeRunSync()

            verify(failingProtocol, never()).addReadGroup(ids)
            verify(failingProtocol).addWriteGroup(ids)

            assertTrue(result.isLeft())
        }
    }
}
