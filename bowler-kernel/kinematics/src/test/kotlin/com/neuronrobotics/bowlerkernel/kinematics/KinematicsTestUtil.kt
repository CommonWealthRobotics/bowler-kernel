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
package com.neuronrobotics.bowlerkernel.kinematics

import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LinkData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.util.Limits
import org.octogonapus.ktguava.collections.immutableListOf

@SuppressWarnings("LongParameterList")
fun createMockLinkData(
    type: LinkType = LinkType.Rotary,
    dhParamData: DhParamData = DhParamData(0.0, 0.0, 0.0, 0.0),
    jointLimits: Limits = Limits(
        1,
        0
    ),
    jointAngleControllerGistId: String = "jacGistId",
    jointAngleControllerFilename: String = "jacFilename",
    inertialStateEstimatorGistId: String = "iseGistId",
    inertialStateEstimatorFilename: String = "iseFilename"
) = LinkData(
    type,
    dhParamData,
    jointLimits,
    GitFile(jointAngleControllerGistId, jointAngleControllerFilename),
    GitFile(inertialStateEstimatorGistId, inertialStateEstimatorFilename)
)

@SuppressWarnings("LongParameterList")
fun createMockLimbData(
    id: SimpleLimbId = SimpleLimbId("limbId"),
    links: List<LinkData> = immutableListOf(createMockLinkData()),
    forwardKinematicsSolverGistId: String = "fksGistId",
    forwardKinematicsSolverFilename: String = "fksFilename",
    inverseKinematicsSolverGistId: String = "iksGistId",
    inverseKinematicsSolverFilename: String = "iksFilename",
    reachabilityCalculatorGistId: String = "reCalcGistId",
    reachabilityCalculatorFilename: String = "reCalcFilename",
    limbMotionPlanGeneratorGistId: String = "lmpgGistId",
    limbMotionPlanGeneratorFilename: String = "lmpgFilename",
    limbMotionPlanFollowerGistId: String = "lmpfGistId",
    limbMotionPlanFollowerFilename: String = "lmpfFilename",
    inertialStateEstimatorGistId: String = "iseGistId",
    inertialStateEstimatorFilename: String = "iseFilename"
) = LimbData(
    id.id,
    links,
    GitFile(forwardKinematicsSolverGistId, forwardKinematicsSolverFilename),
    GitFile(inverseKinematicsSolverGistId, inverseKinematicsSolverFilename),
    GitFile(reachabilityCalculatorGistId, reachabilityCalculatorFilename),
    GitFile(limbMotionPlanGeneratorGistId, limbMotionPlanGeneratorFilename),
    GitFile(limbMotionPlanFollowerGistId, limbMotionPlanFollowerFilename),
    GitFile(inertialStateEstimatorGistId, inertialStateEstimatorFilename)
)

@SuppressWarnings("LongParameterList")
fun createMockKinematicBaseData(
    id: SimpleKinematicBaseId = SimpleKinematicBaseId("kinBaseId"),
    limbs: List<LimbData> = immutableListOf(createMockLimbData()),
    limbTransforms: List<FrameTransformation> = immutableListOf(FrameTransformation.identity),
    bodyControllerGistId: String = "bcGistId",
    bodyControllerFilename: String = "bcFilename"
) = KinematicBaseData(
    id.id,
    limbs,
    limbTransforms,
    GitFile(bodyControllerGistId, bodyControllerFilename)
)
