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
package com.neuronrobotics.bowlerkernel.kinematics.solvers

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.InverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.solvers.solver.NativeIKSolverBridge
import com.neuronrobotics.bowlerkernel.kinematics.solvers.solver.ThreeDofSolver
import com.neuronrobotics.bowlerkernel.util.JointLimits

/**
 * Detects the type of serial manipulator and uses the correct solver for it.
 */
class GeneralInverseKinematicsSolver(
    links: ImmutableList<Link>
) : InverseKinematicsSolver {

    @Suppress("MemberVisibilityCanBePrivate")
    val solver: InverseKinematicsSolver = identifySolver(links)

    override fun solveChain(
        links: List<Link>,
        currentJointAngles: List<Double>,
        jointLimits: List<JointLimits>,
        targetFrameTransform: FrameTransformation
    ): List<Double> = solver.solveChain(
        links,
        currentJointAngles,
        jointLimits,
        targetFrameTransform
    )

    private fun identifySolver(links: List<Link>): InverseKinematicsSolver =
        when (links.size) {
            3 -> ThreeDofSolver() // TODO: Check more than just the number of links
            else -> NativeIKSolverBridge()
        }
}
