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
import arrow.core.fix
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.gitfs.decoder
import com.neuronrobotics.bowlerkernel.gitfs.encoder
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.encoder
import helios.core.DecodingError
import helios.core.JsObject
import helios.core.Json
import helios.core.KeyNotFound
import helios.instances.decoder
import helios.instances.encoder
import helios.typeclasses.Decoder
import helios.typeclasses.Encoder

data class KinematicBaseScriptData(
    val bodyController: Either<GitFile, ClassData>
) {
    companion object
}

fun KinematicBaseScriptData.toJson(): Json = JsObject(mapOf(
    "bodyController" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { bodyController.encode() }
))

fun Json.Companion.toKinematicBaseScriptData(value: Json): Either<DecodingError, KinematicBaseScriptData> =

    value["bodyController"].fold({ Either.Left(KeyNotFound("bodyController")) }, {
        Either.decoder(
            GitFile.decoder(), ClassData.decoder()
        ).run { decode(it) }
    })
        .map { bodyController ->
            KinematicBaseScriptData(bodyController = bodyController)
        }.fix()

fun KinematicBaseScriptData.Companion.encoder() = object : Encoder<KinematicBaseScriptData> {
    override fun KinematicBaseScriptData.encode(): Json = this.toJson()
}

fun KinematicBaseScriptData.Companion.decoder() = object : Decoder<KinematicBaseScriptData> {
    override fun decode(value: Json): Either<DecodingError, KinematicBaseScriptData> =
        Json.toKinematicBaseScriptData(value)
}
