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
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.gitfs.decoder
import com.neuronrobotics.bowlerkernel.gitfs.encoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.encoder
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.encoder
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

data class LimbScriptData(
    val forwardKinematicsSolver: Either<GitFile, ClassData>,
    val inverseKinematicsSolver: Either<GitFile, ClassData>,
    val reachabilityCalculator: Either<GitFile, ClassData>,
    val limbMotionPlanGenerator: Either<GitFile, ClassData>,
    val limbMotionPlanFollower: Either<GitFile, ClassData>,
    val inertialStateEstimator: Either<GitFile, ClassData>,
    val linkScripts: List<LinkScriptData>
) {
    companion object
}

fun LimbScriptData.toJson(): Json = JsObject(mapOf(
    "forwardKinematicsSolver" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { forwardKinematicsSolver.encode() }
    ,
    "inertialStateEstimator" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { inertialStateEstimator.encode() }
    ,
    "inverseKinematicsSolver" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { inverseKinematicsSolver.encode() }
    ,
    "limbMotionPlanFollower" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { limbMotionPlanFollower.encode() }
    ,
    "limbMotionPlanGenerator" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { limbMotionPlanGenerator.encode() }
    ,
    "linkScripts" to ListEncoderInstance(LinkScriptData.encoder()).run { linkScripts.encode() }
    ,
    "reachabilityCalculator" to Either.encoder(
        GitFile.encoder(),
        ClassData.encoder()
    ).run { reachabilityCalculator.encode() }
))

fun Json.Companion.toLimbScriptData(value: Json): Either<DecodingError, LimbScriptData> =
    Either.applicative<DecodingError>().map(
        value["forwardKinematicsSolver"].fold(
            { Either.Left(KeyNotFound("forwardKinematicsSolver")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            }),
        value["inertialStateEstimator"].fold(
            { Either.Left(KeyNotFound("inertialStateEstimator")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            }),
        value["inverseKinematicsSolver"].fold(
            { Either.Left(KeyNotFound("inverseKinematicsSolver")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            }),
        value["limbMotionPlanFollower"].fold(
            { Either.Left(KeyNotFound("limbMotionPlanFollower")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            }),
        value["limbMotionPlanGenerator"].fold(
            { Either.Left(KeyNotFound("limbMotionPlanGenerator")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            }),
        value["linkScripts"].fold({ Either.Left(KeyNotFound("linkScripts")) }, {
            ListDecoderInstance(
                LinkScriptData.decoder()
            ).run { decode(it) }
        }),
        value["reachabilityCalculator"].fold(
            { Either.Left(KeyNotFound("reachabilityCalculator")) },
            {
                Either.decoder(
                    GitFile.decoder(), ClassData.decoder()
                ).run { decode(it) }
            })
    ) { (forwardKinematicsSolver, inertialStateEstimator, inverseKinematicsSolver,
        limbMotionPlanFollower, limbMotionPlanGenerator, linkScripts, reachabilityCalculator) ->
        LimbScriptData(
            forwardKinematicsSolver = forwardKinematicsSolver,
            inertialStateEstimator = inertialStateEstimator,
            inverseKinematicsSolver = inverseKinematicsSolver,
            limbMotionPlanFollower = limbMotionPlanFollower,
            limbMotionPlanGenerator = limbMotionPlanGenerator,
            linkScripts = linkScripts,
            reachabilityCalculator = reachabilityCalculator
        )
    }.fix()

fun LimbScriptData.Companion.encoder() = object : Encoder<LimbScriptData> {
    override fun LimbScriptData.encode(): Json = this.toJson()
}

fun LimbScriptData.Companion.decoder() = object : Decoder<LimbScriptData> {
    override fun decode(value: Json): Either<DecodingError, LimbScriptData> =
        Json.toLimbScriptData(value)
}
