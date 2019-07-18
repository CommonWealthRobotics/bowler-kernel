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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link.model

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.fix
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.decoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.encoder
import helios.core.DecodingError
import helios.core.JsObject
import helios.core.Json
import helios.core.KeyNotFound
import helios.typeclasses.Decoder
import helios.typeclasses.Encoder

data class LinkConfigurationData(
    val type: LinkType,
    val dhParamData: DhParamData
) {
    companion object
}

fun LinkConfigurationData.toJson(): Json = JsObject(mapOf(
    "dhParamData" to DhParamData.encoder().run { dhParamData.encode() },
    "type" to LinkType.encoder().run { type.encode() }
))

fun Json.Companion.toLinkConfigurationData(value: Json): Either<DecodingError, LinkConfigurationData> =
    Either.applicative<DecodingError>().map(
        value["dhParamData"].fold(
            { Either.Left(KeyNotFound("dhParamData")) },
            {
                DhParamData.decoder()
                    .run { decode(it) }
            }),
        value["type"].fold(
            { Either.Left(KeyNotFound("type")) },
            {
                LinkType.decoder()
                    .run { decode(it) }
            })
    ) { (dhParamData, type) ->
        LinkConfigurationData(dhParamData = dhParamData, type = type)
    }.fix()

fun LinkConfigurationData.Companion.encoder() = object : Encoder<LinkConfigurationData> {
    override fun LinkConfigurationData.encode(): Json = this.toJson()
}

fun LinkConfigurationData.Companion.decoder() = object : Decoder<LinkConfigurationData> {
    override fun decode(value: Json): Either<DecodingError, LinkConfigurationData> =
        Json.toLinkConfigurationData(value)
}
