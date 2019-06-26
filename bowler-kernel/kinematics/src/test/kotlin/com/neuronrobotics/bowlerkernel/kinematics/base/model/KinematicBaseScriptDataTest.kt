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
package com.neuronrobotics.bowlerkernel.kinematics.base.model

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkScriptData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.LengthBasedReachabilityCalculator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ClassData
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.ControllerSpecification
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.kinematics.testJsonConversion
import org.junit.jupiter.api.Test

internal class KinematicBaseScriptDataTest {

    @Test
    fun `test json`() {
        val klaxon = Klaxon()

        val linkScriptData = LinkScriptData(
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopJointAngleController,
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopInertialStateEstimator,
                    klaxon
                )
            )
        )

        val limbScriptData = LimbScriptData(
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopForwardKinematicsSolver,
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopInverseKinematicsSolver,
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    LengthBasedReachabilityCalculator(),
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopLimbMotionPlanGenerator,
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopLimbMotionPlanFollower,
                    klaxon
                )
            ),
            ControllerSpecification.fromClassData(
                ClassData.fromInstance(
                    NoopInertialStateEstimator,
                    klaxon
                )
            ),
            listOf(linkScriptData)
        )

        klaxon.testJsonConversion(
            KinematicBaseScriptData(
                ControllerSpecification.fromClassData(
                    ClassData.fromInstance(
                        NoopBodyController,
                        klaxon
                    )
                ),
                listOf(limbScriptData)
            )
        )
    }
}
