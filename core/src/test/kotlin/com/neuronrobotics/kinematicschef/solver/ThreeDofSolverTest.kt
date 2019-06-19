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

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.approxEquals
import com.neuronrobotics.kinematicschef.GeneralForwardKinematicsSolver
import com.neuronrobotics.kinematicschef.TestUtil.hephaestusArmLinks
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.toImmutableList

internal class ThreeDofSolverTest {

    private val fk = GeneralForwardKinematicsSolver()
    private val ik = ThreeDofSolver()

    @Test
    fun `test fk to ik`() {
        (-30..30 step 1).map { targetPos ->
            val target = FrameTransformation.fromTranslation(targetPos, 0, 0)
            val resultAngles = ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.map { 0.0 }.toImmutableList(),
                target
            )
            val resultTarget = fk.solveChain(hephaestusArmLinks, resultAngles)
            assertTrue(target.translation.approxEquals(resultTarget.translation, 1e-10)) {
                """
                Target:
                $target
                Result:
                $resultTarget
                Result angles:
                $resultAngles
                """.trimIndent()
            }
        }
    }

    @Test
    fun `test ik outside reachable workspace`() {
        val target = FrameTransformation.fromTranslation(10000, 0, 0)

        assertThrows<IllegalStateException> {
            ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.map { 0.0 }.toImmutableList(),
                target
            )
        }
    }

    @Test
    fun `test ik with 2 links`() {
        assertThrows<IllegalArgumentException> {
            ik.solveChain(
                hephaestusArmLinks.subList(0, 2),
                hephaestusArmLinks.subList(0, 2).map { 0.0 }.toImmutableList(),
                FrameTransformation.identity
            )
        }
    }

    @Test
    fun `test ik with 3 links and 2 joint angles`() {
        assertThrows<IllegalArgumentException> {
            ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.subList(0, 2).map { 0.0 }.toImmutableList(),
                FrameTransformation.identity
            )
        }
    }
}
