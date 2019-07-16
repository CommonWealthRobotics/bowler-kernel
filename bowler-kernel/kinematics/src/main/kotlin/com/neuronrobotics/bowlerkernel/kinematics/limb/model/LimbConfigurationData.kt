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
package com.neuronrobotics.bowlerkernel.kinematics.limb.model

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.fix
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.encoder
import helios.core.DecodingError
import helios.core.JsObject
import helios.core.Json
import helios.core.KeyNotFound
import helios.instances.ListDecoderInstance
import helios.instances.ListEncoderInstance
import helios.instances.decoder
import helios.instances.encoder
import helios.typeclasses.Decoder
import helios.typeclasses.Encoder

data class LimbConfigurationData(
    val id: String,
    val linkConfigurations: List<LinkConfigurationData>
) {
    companion object
}

fun LimbConfigurationData.toJson(): Json = JsObject(mapOf(
    "id" to String.encoder().run { id.encode() }
    ,
    "linkConfigurations" to ListEncoderInstance(LinkConfigurationData.encoder()).run { linkConfigurations.encode() }
))

fun Json.Companion.toLimbConfigurationData(value: Json): Either<DecodingError, LimbConfigurationData> =
    Either.applicative<DecodingError>().map(
        value["id"].fold(
            { Either.Left(KeyNotFound("id")) },
            { String.decoder().run { decode(it) } }),
        value["linkConfigurations"].fold({ Either.Left(KeyNotFound("linkConfigurations")) }, {
            ListDecoderInstance(
                LinkConfigurationData.decoder()
            ).run { decode(it) }
        })
    ) { (id, linkConfigurations) ->
        LimbConfigurationData(id = id, linkConfigurations = linkConfigurations)
    }.fix()

fun LimbConfigurationData.Companion.encoder() = object : Encoder<LimbConfigurationData> {
    override fun LimbConfigurationData.encode(): Json = this.toJson()
}

fun LimbConfigurationData.Companion.decoder() = object : Decoder<LimbConfigurationData> {
    override fun decode(value: Json): Either<DecodingError, LimbConfigurationData> =
        Json.toLimbConfigurationData(value)
}
