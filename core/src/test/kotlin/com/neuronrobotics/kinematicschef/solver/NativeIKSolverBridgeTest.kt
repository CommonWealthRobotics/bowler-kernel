/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.solver

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.ForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.approxEquals
import com.neuronrobotics.kinematicschef.GeneralForwardKinematicsSolver
import com.neuronrobotics.kinematicschef.TestUtil.hephaestusArmLinks
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.toImmutableList

internal class NativeIKSolverBridgeTest {

    private val fk = GeneralForwardKinematicsSolver()
    private val ik = NativeIKSolverBridge()

    @Test
    fun `test bridge`() {
        (-30..30 step 1).map { targetPos ->
            if (targetPos !in listOf(-16, -5, 0, 1, 6, 7, 8, 15)) {
                val target = FrameTransformation.fromTranslation(targetPos, 0, 0)
                testIK(hephaestusArmLinks, target, ik, fk)
            }
        }
    }

    private fun testIK(
        links: ImmutableList<Link>,
        target: FrameTransformation,
        ik: InverseKinematicsSolver,
        fk: ForwardKinematicsSolver
    ) {
        val resultAngles = ik.solveChain(links, links.map { 0.0 }.toImmutableList(), target)
        val resultTarget = fk.solveChain(hephaestusArmLinks, resultAngles)
        assertTrue(
            target.translation.approxEquals(resultTarget.translation, 1e-1)
        ) {
            """
                Target:
                ${target.translation.array.joinToString { it.joinToString() }}
                Result:
                ${resultTarget.translation.array.joinToString { it.joinToString() }}
                Result angles :
                $resultAngles
                """.trimIndent()
        }
    }
}
