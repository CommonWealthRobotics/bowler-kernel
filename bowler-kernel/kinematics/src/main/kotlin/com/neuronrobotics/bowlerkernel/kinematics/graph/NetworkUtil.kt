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

import arrow.core.Tuple3
import arrow.data.ListK
import arrow.data.extensions.listk.applicative.applicative
import arrow.data.fix
import arrow.data.k
import com.google.common.graph.Network

@Suppress("UnstableApiUsage")
fun <A, B> Network<A, B>.fullEdges(): Set<Tuple3<A, A, B>> {
    val nodes = nodes().toList()
    return ListK.applicative()
        .tupled(nodes.k(), nodes.k())
        .fix()
        .mapNotNull { (nodeU, nodeV) ->
            val edge = edgeConnecting(nodeU, nodeV)
            if (edge.isPresent) {
                Tuple3(nodeU, nodeV, edge.get())
            } else {
                null
            }
        }
        .toSet()
}
