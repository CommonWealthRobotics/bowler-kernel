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

import com.commonwealthrobotics.bowlerkernel.deviceserver.DefaultDeviceServer
import com.commonwealthrobotics.bowlerkernel.deviceserver.UDPTransportLayer
import com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid.ConnectionMethod
import com.commonwealthrobotics.bowlerkernel.hardware.device.deviceid.DeviceId

/**
 * A [BowlerRPCProtocolFactory] which makes [DefaultBowlerRPCProtocol]. Supports
 * [ConnectionMethod.InternetAddress] and [ConnectionMethod.RawHID].
 */
class DefaultBowlerRPCProtocolFactory : BowlerRPCProtocolFactory {

    override fun create(deviceId: DeviceId) =
        when (val connectionMethod = deviceId.connectionMethod) {
            is ConnectionMethod.InternetAddress ->
                DefaultBowlerRPCProtocol(
                    server = DefaultDeviceServer(
                        UDPTransportLayer(
                            connectionMethod.inetAddress,
                            1866, // TODO: This port should be configurable
                            DefaultBowlerRPCProtocol.PACKET_SIZE
                        ),
                        DefaultBowlerRPCProtocol.PAYLOAD_SIZE
                    ),
                    startPacketId = DefaultBowlerRPCProtocol.DEFAULT_START_PACKET_ID
                )

            is ConnectionMethod.DeviceName -> TODO()

            is ConnectionMethod.RawHID -> TODO()
        }
}
