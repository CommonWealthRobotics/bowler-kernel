/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics

import com.neuronrobotics.bowlerkernel.kinematics.Limits
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LinkData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.util.immutableListOf

@SuppressWarnings("LongParameterList")
fun createMockLinkData(
    type: LinkType = LinkType.Rotary,
    dhParamData: DhParamData = DhParamData(0.0, 0.0, 0.0, 0.0),
    jointLimits: Limits = Limits(
        1.0,
        0.0
    ),
    jointAngleControllerGistId: String = "jacGistId",
    jointAngleControllerFilename: String = "jacFilename",
    inertialStateEstimatorGistId: String = "iseGistId",
    inertialStateEstimatorFilename: String = "iseFilename"
) = LinkData(
    type,
    dhParamData,
    jointLimits,
    jointAngleControllerGistId,
    jointAngleControllerFilename,
    inertialStateEstimatorGistId,
    inertialStateEstimatorFilename
)

@SuppressWarnings("LongParameterList")
fun createMockLimbData(
    id: SimpleLimbId = SimpleLimbId("limbId"),
    links: List<LinkData> = immutableListOf(createMockLinkData()),
    forwardKinematicsSolverGistId: String = "fksGistId",
    forwardKinematicsSolverFilename: String = "fksFilename",
    inverseKinematicsSolverGistId: String = "iksGistId",
    inverseKinematicsSolverFilename: String = "iksFilename",
    limbMotionPlanGeneratorGistId: String = "lmpgGistId",
    limbMotionPlanGeneratorFilename: String = "lmpgFilename",
    limbMotionPlanFollowerGistId: String = "lmpfGistId",
    limbMotionPlanFollowerFilename: String = "lmpfFilename",
    inertialStateEstimatorGistId: String = "iseGistId",
    inertialStateEstimatorFilename: String = "iseFilename"
) = LimbData(
    id,
    links,
    forwardKinematicsSolverGistId,
    forwardKinematicsSolverFilename,
    inverseKinematicsSolverGistId,
    inverseKinematicsSolverFilename,
    limbMotionPlanGeneratorGistId,
    limbMotionPlanGeneratorFilename,
    limbMotionPlanFollowerGistId,
    limbMotionPlanFollowerFilename,
    inertialStateEstimatorGistId,
    inertialStateEstimatorFilename
)

@SuppressWarnings("LongParameterList")
fun createMockKinematicBaseData(
    id: SimpleKinematicBaseId = SimpleKinematicBaseId("kinBaseId"),
    limbs: List<LimbData> = immutableListOf(createMockLimbData()),
    limbTransforms: List<FrameTransformation> = immutableListOf(FrameTransformation.identity()),
    bodyControllerGistId: String = "bcGistId",
    bodyControllerFilename: String = "bcFilename"
) = KinematicBaseData(id, limbs, limbTransforms, bodyControllerGistId, bodyControllerFilename)
