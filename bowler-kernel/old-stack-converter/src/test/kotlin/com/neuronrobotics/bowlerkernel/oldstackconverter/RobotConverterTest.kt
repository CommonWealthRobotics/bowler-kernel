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
package com.neuronrobotics.bowlerkernel.oldstackconverter

import arrow.core.Either
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopJointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.Math.toDegrees

internal class RobotConverterTest {

    @Test
    fun `test converting 3001 arm to kinematic graph`() {
        val mobileBase = ScriptingEngine.gitScriptRun(
            "https://github.com/madhephaestus/SeriesElasticActuator.git",
            "seaArm.xml",
            null
        ) as MobileBase
        val oldLimb = mobileBase.appendages.first()

        val converter = RobotConverter(mobileBase)
        val result = converter.convertToKinematicGraph(
            { _, _ -> NoopLimbMotionPlanGenerator },
            { _, _ -> NoopLimbMotionPlanFollower },
            { _, _, _, _ -> NoopJointAngleController },
            { _, _, _ -> NoopInertialStateEstimator },
            { _, _ -> NoopInertialStateEstimator }
        )

        assertThat(result.nodes(), hasSize(equalTo(2)))
        val baseNode = (result.nodes().first { it is Either.Left } as Either.Left).a
        val limbNode = (result.nodes().first { it is Either.Right } as Either.Right).b

        assertAll(
            {
                assertEquals(SimpleKinematicBaseId(mobileBase.scriptingName), baseNode.id)
            },
            {
                assertEquals(SimpleLimbId(oldLimb.scriptingName), limbNode.id)
            },
            {
                assertEquals(
                    mobileBase.allDHChains.first().chain.links.map { mapToDefaultLink(it) },
                    limbNode.links
                )
            },
            {
                assertEquals(
                    FrameTransformation.fromMatrix(
                        oldLimb.robotToFiducialTransform.matrixTransform
                    ),
                    result.edges().first()
                )
            }
        )
    }

    @Test
    fun `test converting 3001 arm to kinematic base`() {
        val mobileBase = ScriptingEngine.gitScriptRun(
            "https://github.com/madhephaestus/SeriesElasticActuator.git",
            "seaArm.xml",
            null
        ) as MobileBase
        val oldLimb = mobileBase.appendages.first()

        val converter = RobotConverter(mobileBase)
        val result = converter.convertToKinematicBase(
            NoopBodyController,
            { _, _ -> NoopLimbMotionPlanGenerator },
            { _, _ -> NoopLimbMotionPlanFollower },
            { _, _, _, _ -> NoopJointAngleController },
            { _, _, _ -> NoopInertialStateEstimator },
            { _, _ -> NoopInertialStateEstimator }
        ) as DefaultKinematicBase

        assertAll(
            {
                assertEquals(SimpleKinematicBaseId(mobileBase.scriptingName), result.id)
            },
            {
                assertEquals(SimpleLimbId(oldLimb.scriptingName), result.limbs.first().id)
            },
            {
                assertEquals(
                    mobileBase.allDHChains.first().chain.links.map { mapToDefaultLink(it) },
                    result.limbs.first().links
                )
            },
            {
                assertEquals(
                    FrameTransformation.fromMatrix(
                        oldLimb.robotToFiducialTransform.matrixTransform
                    ),
                    result.limbBaseTransforms.values.first()
                )
            }
        )
    }

    private fun mapToDefaultLink(dhLink: DHLink): DefaultLink {
        return DefaultLink(
            LinkType.Rotary,
            DhParam(dhLink.d, toDegrees(dhLink.theta), dhLink.r, toDegrees(dhLink.alpha)),
            NoopInertialStateEstimator
        )
    }
}
