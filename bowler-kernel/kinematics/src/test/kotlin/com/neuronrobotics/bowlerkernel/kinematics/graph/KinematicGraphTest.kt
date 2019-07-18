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
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimbFactory
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLinkFactory
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableNetwork

@Suppress("UnstableApiUsage")
internal class KinematicGraphTest {

    @Test
    fun `test json conversion`() {
        val mutableKinematicGraph = buildMutableKinematicGraph()

        val baseNodeA = BaseNode(SimpleKinematicBaseId("BaseA")).left()

        val limbA = makeLimb("LimbA")
        val limbB = makeLimb("LimbB")

        mutableKinematicGraph.addEdge(
            baseNodeA,
            limbA,
            FrameTransformation.fromTranslation(10, 0, -10)
        )

        mutableKinematicGraph.addEdge(
            baseNodeA,
            limbB,
            FrameTransformation.fromTranslation(-10, 0, -10)
        )

        val kinematicGraph = mutableKinematicGraph.toImmutableNetwork()

        val baseA = DefaultKinematicBase.create(
            kinematicGraph,
            SimpleKinematicBaseId("BaseA"),
            NoopBodyController
        )

        val kinematicGraphData = kinematicGraph.convertToKinematicGraphData(setOf(baseA))

        val graphJson = KinematicGraphData.encoder().run { kinematicGraphData.encode() }

        val decodedGraph = graphJson.decode(KinematicGraphData.decoder())
        assertTrue(decodedGraph.isRight())
        decodedGraph as Either.Right

        val convertedKinematicGraph = decodedGraph.b.convertToKinematicGraph(
            DefaultLimbFactory(
                mock {},
                DefaultLinkFactory(mock {})
            )
        )

        assertTrue(convertedKinematicGraph is Either.Right)
        convertedKinematicGraph as Either.Right

        assertEquals(kinematicGraph.fullEdges(), convertedKinematicGraph.b.fullEdges())
    }

    private fun makeLimb(name: String) = DefaultLimb(
        SimpleLimbId(name),
        seaArmLinks,
        NoopForwardKinematicsSolver,
        NoopInverseKinematicsSolver,
        LengthBasedReachabilityCalculator(),
        NoopLimbMotionPlanGenerator,
        NoopLimbMotionPlanFollower,
        seaArmLinks.map {
            NoopJointAngleController
        }.toImmutableList(),
        NoopInertialStateEstimator
    ).right()
}
