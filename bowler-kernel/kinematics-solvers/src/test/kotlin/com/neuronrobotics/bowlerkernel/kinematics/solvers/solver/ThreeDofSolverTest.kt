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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.solver

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.solvers.GeneralForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.solvers.TestUtil.hephaestusArmLinks
import com.neuronrobotics.bowlerkernel.util.JointLimits
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class ThreeDofSolverTest {

    private val fk = GeneralForwardKinematicsSolver()
    private val ik = ThreeDofSolver()

    @Test
    fun `test fk to ik`() {
        (-30..30 step 1).map { targetPos ->
            val target = FrameTransformation.fromTranslation(targetPos, 0, 0)
            testIK(hephaestusArmLinks, target, ik, fk)
        }
    }

    @Test
    fun `test ik outside reachable workspace`() {
        val target = FrameTransformation.fromTranslation(10000, 0, 0)

        assertThrows<IllegalStateException> {
            ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.map { 0.0 },
                hephaestusArmLinks.map { JointLimits(180, -180) },
                target
            )
        }
    }

    @Test
    fun `test ik with 2 links`() {
        assertThrows<IllegalArgumentException> {
            ik.solveChain(
                hephaestusArmLinks.subList(0, 2),
                hephaestusArmLinks.subList(0, 2).map { 0.0 },
                hephaestusArmLinks.subList(0, 2).map { JointLimits(180, -180) },
                FrameTransformation.identity
            )
        }
    }

    @Test
    fun `test ik with 3 links and 2 joint angles`() {
        assertThrows<IllegalArgumentException> {
            ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.subList(0, 2).map { 0.0 },
                hephaestusArmLinks.map { JointLimits(180, -180) },
                FrameTransformation.identity
            )
        }
    }

    @Test
    fun `test ik with 3 links and 2 joint limits`() {
        assertThrows<IllegalArgumentException> {
            ik.solveChain(
                hephaestusArmLinks,
                hephaestusArmLinks.map { 0.0 },
                hephaestusArmLinks.subList(0, 2).map { JointLimits(180, -180) },
                FrameTransformation.identity
            )
        }
    }
}
