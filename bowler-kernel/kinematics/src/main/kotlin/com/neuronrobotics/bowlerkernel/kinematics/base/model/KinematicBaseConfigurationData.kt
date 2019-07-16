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
package com.neuronrobotics.bowlerkernel.kinematics.base.model

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.fix
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.encoder
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
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

data class KinematicBaseConfigurationData(
    val id: String,
    val limbConfigurations: List<LimbConfigurationData>,
    val limbTransforms: List<FrameTransformation>
) {
    init {
        require(limbConfigurations.size == limbTransforms.size) {
            """
            Must have an equal number of limb configurations and limb transforms.
            Limb configurations size: $limbConfigurations
            Limb transforms size: $limbTransforms
            """.trimIndent()
        }
    }

    companion object
}

fun KinematicBaseConfigurationData.toJson(): Json = JsObject(mapOf(
    "id" to String.encoder().run { id.encode() }
    ,
    "limbConfigurations" to ListEncoderInstance(LimbConfigurationData.encoder()).run { limbConfigurations.encode() }
    ,
    "limbTransforms" to ListEncoderInstance(FrameTransformation.encoder()).run { limbTransforms.encode() }
))

fun Json.Companion.toKinematicBaseConfigurationData(value: Json): Either<DecodingError, KinematicBaseConfigurationData> =
    Either.applicative<DecodingError>().map(
        value["id"].fold(
            { Either.Left(KeyNotFound("id")) },
            { String.decoder().run { decode(it) } }),
        value["limbConfigurations"].fold({ Either.Left(KeyNotFound("limbConfigurations")) }, {
            ListDecoderInstance(
                LimbConfigurationData.decoder()
            ).run { decode(it) }
        }),
        value["limbTransforms"].fold({ Either.Left(KeyNotFound("limbTransforms")) }, {
            ListDecoderInstance(
                FrameTransformation.decoder()
            ).run { decode(it) }
        })
    ) { (id, limbConfigurations, limbTransforms) ->
        KinematicBaseConfigurationData(
            id = id,
            limbConfigurations = limbConfigurations,
            limbTransforms = limbTransforms
        )
    }.fix()

fun KinematicBaseConfigurationData.Companion.encoder() = object :
    Encoder<KinematicBaseConfigurationData> {
    override fun KinematicBaseConfigurationData.encode(): Json = this.toJson()
}

fun KinematicBaseConfigurationData.Companion.decoder() = object :
    Decoder<KinematicBaseConfigurationData> {
    override fun decode(value: Json): Either<DecodingError, KinematicBaseConfigurationData> =
        Json.toKinematicBaseConfigurationData(value)
}
