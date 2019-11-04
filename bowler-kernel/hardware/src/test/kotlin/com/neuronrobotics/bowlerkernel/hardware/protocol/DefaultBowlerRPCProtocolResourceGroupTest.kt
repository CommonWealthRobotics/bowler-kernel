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
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod", "LongMethod")

package com.neuronrobotics.bowlerkernel.hardware.protocol

import arrow.effects.IO
import arrow.syntax.function.partially3
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.deviceserver.getPayload
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceIdValidator
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultBowlerRPCProtocolResourceGroupTest {

    private val server = MockDeviceServer()
    private val validator = DefaultResourceIdValidator()

    private val protocol = DefaultBowlerRPCProtocol(
        server = server,
        resourceIdValidator = validator
    )

    @ParameterizedTest
    @MethodSource("defaultResourceTypesSource")
    fun `test DefaultResourceTypes send and receive lengths`(resourceType: DefaultResourceTypes) {
        val addMethod: DefaultBowlerRPCProtocol.(
            resourceIds: ImmutableSet<ResourceId>
        ) -> IO<Unit> =
            when {
                validator.validateIsReadType(resourceType).isRight() ->
                    DefaultBowlerRPCProtocol::addReadGroup.partially3(false)

                validator.validateIsWriteType(resourceType).isRight() ->
                    DefaultBowlerRPCProtocol::addWriteGroup.partially3(false)

                else -> fail { "Unknown resource type: $resourceType" }
            }

        protocolTest(protocol, server) {
            operation {
                val result = it.addMethod(
                    immutableSetOf(
                        ResourceId(
                            resourceType,
                            DefaultAttachmentPoints.Pin(32)
                        )
                    )
                ).attempt().unsafeRunSync()
                assertTrue(result.isRight())
            } pcSends {
                immutableListOf(
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_DISCOVERY_ID,
                            1,
                            DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID,
                            1,
                            DefaultBowlerRPCProtocol.UNRELIABLE_TRANSPORT
                        )
                    ),
                    getPayload(
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE,
                        byteArrayOf(
                            DefaultBowlerRPCProtocol.OPERATION_GROUP_MEMBER_DISCOVERY_ID,
                            1,
                            0,
                            resourceType.sendLength,
                            0,
                            resourceType.receiveLength,
                            resourceType.type,
                            1,
                            32
                        )
                    )
                )
            } deviceResponds {
                immutableListOf(
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
        fun defaultResourceTypesSource() =
            DefaultResourceTypes::class.nestedClasses
                .filter { !it.isCompanion && it.objectInstance != null }.map { it.objectInstance }
    }
}
