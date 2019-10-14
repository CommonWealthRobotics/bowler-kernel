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
@file:SuppressWarnings("LongMethod")

package com.neuronrobotics.bowlerkernel.kinematics.graph

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.randomFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableNetwork
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class KinematicGraphTest {

    private val mutableKinematicGraph = buildMutableKinematicGraph()

    private val baseNodeA = BaseNode(SimpleKinematicBaseId("BaseA")).left()

    private val limbA = makeLimb("LimbA")
    private val limbB = makeLimb("LimbB")
    private val limbABaseTransform = FrameTransformation.fromTranslation(10, 0, -10)
    private val limbBBaseTransform = FrameTransformation.fromTranslation(-10, 0, -10)

    private lateinit var kinematicGraph: KinematicGraph

    @BeforeEach
    fun beforeEach() {
        mutableKinematicGraph.addEdge(baseNodeA, limbA, limbABaseTransform)
        mutableKinematicGraph.addEdge(baseNodeA, limbB, limbBBaseTransform)
        kinematicGraph = mutableKinematicGraph.toImmutableNetwork()
    }

    @Test
    fun `test create`() {
        val baseId = SimpleKinematicBaseId("BaseA")
        val bodyController = NoopBodyController
        val imuTransform = randomFrameTransformation()
        val base = DefaultKinematicBase.create(
            kinematicGraph,
            baseId,
            bodyController,
            imuTransform
        )

        assertAll(
            { assertEquals(setOf(limbA.b, limbB.b), base.limbs) },
            { assertEquals(baseId, base.id) },
            { assertEquals(bodyController, base.bodyController) },
            {
                assertEquals(
                    mapOf(limbA.b.id to limbABaseTransform, limbB.b.id to limbBBaseTransform),
                    base.limbBaseTransforms
                )
            },
            { assertEquals(imuTransform, base.imuTransform) }
        )
    }

    @Test
    fun `test json conversion`() {
        val baseA = DefaultKinematicBase.create(
            kinematicGraph,
            SimpleKinematicBaseId("BaseA"),
            NoopBodyController,
            FrameTransformation.identity
        )

        val kinematicGraphData = kinematicGraph.convertToKinematicGraphData(setOf(baseA))

        val graphJson = KinematicGraphData.encoder().run { kinematicGraphData.encode() }

        val decodedGraph = graphJson.decode(KinematicGraphData.decoder())
        assertTrue(decodedGraph.isRight()) {
            """
            Expected Either.Right, got:
            $decodedGraph
            """.trimIndent()
        }

        decodedGraph as Either.Right
        val convertedKinematicGraph = decodedGraph.b.convertToKinematicGraph(mock {
            on { createLimb(any(), any()) } doReturnConsecutively listOf(limbA, limbB)
        })

        assertTrue(convertedKinematicGraph is Either.Right) {
            """
            Expected Either.Right, got:
            $convertedKinematicGraph
            """.trimIndent()
        }

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
    ).right() as Either.Right
}
