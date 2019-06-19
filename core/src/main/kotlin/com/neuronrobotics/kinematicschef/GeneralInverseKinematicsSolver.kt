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
package com.neuronrobotics.kinematicschef

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.kinematicschef.solver.ThreeDofSolver

/**
 * Detects the type of chain and uses the correct solver for it.
 */
class GeneralInverseKinematicsSolver(
    private val links: ImmutableList<Link>
) {

    private val solver: InverseKinematicsSolver

    init {
        solver = when (links.size) {
            3 -> ThreeDofSolver()
            else -> TODO("Unsupported number of links.")
        }
    }

    /**
     * Solve the system to produce new joint angles. Uses the links from the constructor.
     *
     * @param currentJointAngles The current joint angles.
     * @param targetFrameTransform The target task space frame transformation for the chain.
     * @return New joint angles to reach the [targetFrameTransform].
     */
    fun solveChain(
        currentJointAngles: ImmutableList<Double>,
        targetFrameTransform: FrameTransformation
    ) = solver.solveChain(links, currentJointAngles, targetFrameTransform)
}
