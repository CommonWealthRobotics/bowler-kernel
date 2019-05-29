/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.util.Limits
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableMap

internal fun createMockKinematicBase(limbs: ImmutableList<ImmutableList<DhParam>>): KinematicBase {
    return DefaultKinematicBase(
        SimpleKinematicBaseId(""),
        limbs.mapIndexed { index, limb ->
            DefaultLimb(
                SimpleLimbId(index.toString()),
                limb.map { linkParam ->
                    DefaultLink(
                        LinkType.Rotary,
                        linkParam,
                        Limits(180, -180),
                        NoopInertialStateEstimator
                    )
                }.toImmutableList(),
                NoopForwardKinematicsSolver,
                NoopInverseKinematicsSolver,
                LengthBasedReachabilityCalculator(),
                NoopLimbMotionPlanGenerator,
                NoopLimbMotionPlanFollower,
                limb.map { NoopJointAngleController }.toImmutableList(),
                NoopInertialStateEstimator
            )
        }.toImmutableList(),
        limbs.mapIndexed { index, _ ->
            SimpleLimbId(index.toString()) to FrameTransformation.identity
        }.toImmutableMap(),
        NoopBodyController
    )
}

internal val cmmInputArmDhParams = immutableListOf(
    DhParam(13, 180, 32, -90),
    DhParam(25, -90, 93, 180),
    DhParam(11, 90, 24, 90),
    DhParam(128, -90, 0, 90),
    DhParam(0, 0, 0, -90),
    DhParam(25, 90, 0, 0)
)
