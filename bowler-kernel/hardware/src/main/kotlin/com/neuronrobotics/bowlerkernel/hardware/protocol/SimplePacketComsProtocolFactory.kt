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
package com.neuronrobotics.bowlerkernel.hardware.protocol

import com.neuronrobotics.bowlerkernel.deviceserver.DefaultDeviceServer
import com.neuronrobotics.bowlerkernel.deviceserver.UDPTransportLayer
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DefaultConnectionMethods
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceIdValidator
import mu.KotlinLogging

/**
 * A [BowlerRPCProtocolFactory] which makes [SimplePacketComsProtocol]. Supports
 * [DefaultConnectionMethods.InternetAddress] and [DefaultConnectionMethods.RawHID].
 *
 * @param resourceIdValidator The resource id validator to give to the [SimplePacketComsProtocol].
 */
class SimplePacketComsProtocolFactory(
    private val resourceIdValidator: ResourceIdValidator
) : BowlerRPCProtocolFactory {

    override fun create(deviceId: DeviceId) =
        create(deviceId, SimplePacketComsProtocol.DEFAULT_START_PACKET_ID)

    /**
     * Creates a new [SimplePacketComsProtocol].
     *
     * @param deviceId The device id.
     * @param startPacketId The start packet id.
     * @return The new [SimplePacketComsProtocol].
     */
    fun create(deviceId: DeviceId, startPacketId: Byte): BowlerRPCProtocol {
        val connectionMethod = deviceId.connectionMethod

        return if (connectionMethod is DefaultConnectionMethods) {
            when (connectionMethod) {
                is DefaultConnectionMethods.InternetAddress ->
                    SimplePacketComsProtocol(
                        server = DefaultDeviceServer(
                            UDPTransportLayer(
                                connectionMethod.inetAddress,
                                1866,
                                SimplePacketComsProtocol.PACKET_SIZE
                            )
                        ),
                        startPacketId = startPacketId,
                        resourceIdValidator = resourceIdValidator
                    )

                is DefaultConnectionMethods.DeviceName -> TODO()

                is DefaultConnectionMethods.RawHID -> TODO()
            }
        } else {
            throw UnsupportedOperationException(
                """
                |Cannot construct a SimplePacketComsProtocol from deviceId:
                |$deviceId
                """.trimMargin()
            )
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
