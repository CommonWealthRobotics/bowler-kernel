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
