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
package com.neuronrobotics.bowlerkernel.kinematics.graph

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.monad.binding
import arrow.core.fix
import arrow.core.left
import com.google.common.graph.NetworkBuilder
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.base.model.decoder
import com.neuronrobotics.bowlerkernel.kinematics.base.model.encoder
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.LimbFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
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
import org.octogonapus.ktguava.collections.toImmutableNetwork

typealias KinematicGraphDataNode = Either<
    Tuple2<KinematicBaseConfigurationData, KinematicBaseScriptData>,
    Tuple2<LimbConfigurationData, LimbScriptData>>

/**
 * The data represented by a kinematic graph, plus the actual kinematic bases.
 */
data class KinematicGraphData(
    val nodes: List<KinematicGraphDataNode>,
    val edges: List<Tuple3<KinematicGraphDataNode, KinematicGraphDataNode, FrameTransformation>>
) {

    @Suppress("UnstableApiUsage")
    fun convertToKinematicGraph(
        limbFactory: LimbFactory
    ): Either<String, KinematicGraph> =
        binding {
            val mutableNetwork = NetworkBuilder.directed()
                .allowsParallelEdges(false)
                .expectedNodeCount(nodes.size)
                .expectedEdgeCount(edges.size)
                .allowsSelfLoops(false)
                .build<Either<BaseNode, Limb>, FrameTransformation>()

            val mappedNodes = nodes.map {
                it to it.bimap(
                    {
                        BaseNode(
                            SimpleKinematicBaseId(
                                it.a.id
                            )
                        )
                    },
                    {
                        limbFactory.createLimb(it.a, it.b).bind()
                    }
                )
            }.toMap()

            edges.forEach { (nodeU, nodeV, edge) ->
                try {
                    mutableNetwork.addEdge(
                        mappedNodes[nodeU] ?: error(""),
                        mappedNodes[nodeV] ?: error(""),
                        edge
                    )
                } catch (e: IllegalArgumentException) {
                    @Suppress("RemoveExplicitTypeArguments")
                    e.localizedMessage.left().bind<Nothing>()
                }
            }

            mutableNetwork.toImmutableNetwork()
        }

    companion object
}

fun KinematicGraphData.toJson(): Json = JsObject(mapOf(
    "edges" to ListEncoderInstance(
        Tuple3.encoder(
            Either.encoder(
                Tuple2.encoder(
                    KinematicBaseConfigurationData.encoder(),
                    KinematicBaseScriptData.encoder()
                ),
                Tuple2.encoder(
                    LimbConfigurationData.encoder(),
                    LimbScriptData.encoder()
                )
            ),
            Either.encoder(
                Tuple2.encoder(
                    KinematicBaseConfigurationData.encoder(),
                    KinematicBaseScriptData.encoder()
                ),
                Tuple2.encoder(
                    LimbConfigurationData.encoder(),
                    LimbScriptData.encoder()
                )
            ),
            FrameTransformation.encoder()
        )
    ).run { edges.encode() },
    "nodes" to ListEncoderInstance(
        Either.encoder(
            Tuple2.encoder(
                KinematicBaseConfigurationData.encoder(),
                KinematicBaseScriptData.encoder()
            ),
            Tuple2.encoder(
                LimbConfigurationData.encoder(),
                LimbScriptData.encoder()
            )
        )
    ).run { nodes.encode() }
))

fun Json.Companion.toKinematicGraphData(value: Json): Either<DecodingError, KinematicGraphData> =
    Either.applicative<DecodingError>().map(
        value["edges"].fold({ Either.Left(KeyNotFound("edges")) }, {
            ListDecoderInstance(
                Tuple3.decoder(
                    Either.decoder(
                        Tuple2.decoder(
                            KinematicBaseConfigurationData.decoder(),
                            KinematicBaseScriptData.decoder()
                        ),
                        Tuple2.decoder(
                            LimbConfigurationData.decoder(),
                            LimbScriptData.decoder()
                        )
                    ),
                    Either.decoder(
                        Tuple2.decoder(
                            KinematicBaseConfigurationData.decoder(),
                            KinematicBaseScriptData.decoder()
                        ),
                        Tuple2.decoder(
                            LimbConfigurationData.decoder(),
                            LimbScriptData.decoder()
                        )
                    ),
                    FrameTransformation.decoder()
                )
            ).run { decode(it) }
        }),
        value["nodes"].fold(
            { Either.Left(KeyNotFound("nodes")) },
            {
                ListDecoderInstance(
                    Either.decoder(
                        Tuple2.decoder(
                            KinematicBaseConfigurationData.decoder(),
                            KinematicBaseScriptData.decoder()
                        ),
                        Tuple2.decoder(
                            LimbConfigurationData.decoder(),
                            LimbScriptData.decoder()
                        )
                    )
                ).run { decode(it) }
            })
    ) { (edges, nodes) ->
        KinematicGraphData(edges = edges, nodes = nodes)
    }.fix()

fun KinematicGraphData.Companion.encoder() = object : Encoder<KinematicGraphData> {
    override fun KinematicGraphData.encode(): Json = this.toJson()
}

fun KinematicGraphData.Companion.decoder() = object : Decoder<KinematicGraphData> {
    override fun decode(value: Json): Either<DecodingError, KinematicGraphData> =
        Json.toKinematicGraphData(value)
}
