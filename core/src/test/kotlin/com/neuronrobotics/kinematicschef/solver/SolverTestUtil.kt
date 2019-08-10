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
import com.neuronrobotics.bowlerkernel.util.JointLimits
import com.neuronrobotics.kinematicschef.TestUtil
import org.junit.jupiter.api.Assertions.assertTrue

internal fun testIK(
    links: ImmutableList<Link>,
    target: FrameTransformation,
    ik: InverseKinematicsSolver,
    fk: ForwardKinematicsSolver
) {
    val resultAngles = ik.solveChain(
        links,
        links.map { 0.0 },
        links.map { JointLimits(180, -180) },
        target
    )

    val resultTarget = fk.solveChain(TestUtil.hephaestusArmLinks, resultAngles)

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
