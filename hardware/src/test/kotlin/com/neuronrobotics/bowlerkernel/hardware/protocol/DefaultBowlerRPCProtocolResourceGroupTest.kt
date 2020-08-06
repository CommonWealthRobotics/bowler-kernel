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
package com.commonwealthrobotics.bowlerkernel.hardware.protocol

import com.commonwealthrobotics.bowlerkernel.deviceserver.getPayload
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.AttachmentPoint
import com.commonwealthrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolResourceGroupTest {

    private val server = MockDeviceServer()
    private val protocol = DefaultBowlerRPCProtocol(server)

    @ParameterizedTest
    @MethodSource("defaultResourceTypesSource")
    fun `test DefaultResourceTypes send and receive lengths`(resourceType: ResourceId) {
        protocolTest(protocol, server) {
            operation {
                val result = it.addGroup(listOf(resourceType)).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            1
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            resourceType.resourceType.sendLength,
                            0,
                            resourceType.resourceType.receiveLength,
                            resourceType.resourceType.type,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                listOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(DefaultBowlerRPCProtocol.STATUS_ACCEPTED)
                    )
                )
            }
        }
    }

    companion object {

        @Suppress("UNUSED")
        @JvmStatic
        fun defaultResourceTypesSource() = listOf(
            ResourceId(digitalOut, AttachmentPoint.Pin(32)),
            ResourceId(analogIn, AttachmentPoint.Pin(32)),
            ResourceId(serial, AttachmentPoint.Pin(32))
        )
    }
}
