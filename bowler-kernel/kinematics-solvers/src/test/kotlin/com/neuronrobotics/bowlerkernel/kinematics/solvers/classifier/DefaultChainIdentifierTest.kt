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
@file:SuppressWarnings("LargeClass", "TooManyFunctions", "LongMethod")

package com.neuronrobotics.bowlerkernel.kinematics.solvers.classifier

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.solvers.TestUtil
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.DhChainElement
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.RevoluteJoint
import com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam.SphericalWrist
import com.neuronrobotics.bowlerkernel.kinematics.solvers.not
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.plus

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultChainIdentifierTest {

    @Test
    fun `test single revolute joint`() {
        val chain = TestUtil.randomDhParamList(1)

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(chain) } doReturn Option.just("")
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf<DhChainElement>(RevoluteJoint(chain)),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test two revolute joints`() {
        val pin1 = TestUtil.randomDhParamList(1)
        val pin2 = TestUtil.randomDhParamList(1)
        val chain = pin1 + pin2

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(chain) } doReturn Option.just("")
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf<DhChainElement>(RevoluteJoint(pin1), RevoluteJoint(pin2)),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test single spherical wrist`() {
        val chain = TestUtil.randomDhParamList(3)

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(chain) } doReturn Option.empty()
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf<DhChainElement>(SphericalWrist(chain)),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test spherical wrist and revolute joint`() {
        val wrist = TestUtil.randomDhParamList(3)
        val pin = TestUtil.randomDhParamList(1)
        val chain = wrist + pin

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(wrist) } doReturn Option.empty()
            on { isSphericalWrist(not(wrist)) } doReturn Option.just("")
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf(SphericalWrist(wrist), RevoluteJoint(pin)),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test revolute joint, spherical wrist, and revolute joint`() {
        val wrist = TestUtil.randomDhParamList(3)
        val pin = TestUtil.randomDhParamList(1)
        val pin2 = TestUtil.randomDhParamList(1)
        val chain = pin + wrist + pin2

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(wrist) } doReturn Option.empty()
            on { isSphericalWrist(not(wrist)) } doReturn Option.just("")
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf(
                RevoluteJoint(pin),
                SphericalWrist(wrist),
                RevoluteJoint(pin2)
            ),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test three revolute joints and a spherical wrist`() {
        val pin = TestUtil.randomDhParamList(1)
        val pin2 = TestUtil.randomDhParamList(1)
        val pin3 = TestUtil.randomDhParamList(1)
        val wrist = TestUtil.randomDhParamList(3)
        val chain = pin + pin2 + pin3 + wrist

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(wrist) } doReturn Option.empty()
            on { isSphericalWrist(not(wrist)) } doReturn Option.just("")
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf(
                RevoluteJoint(pin),
                RevoluteJoint(pin2),
                RevoluteJoint(pin3),
                SphericalWrist(wrist)
            ),
            identifier.identifyChain(chain)
        )
    }

    @Test
    fun `test spherical wrist, revolute joint, and a spherical wrist`() {
        val wrist = TestUtil.randomDhParamList(3)
        val pin = TestUtil.randomDhParamList(1)
        val wrist2 = TestUtil.randomDhParamList(3)
        val chain = wrist + pin + wrist2

        val mockWristIdentifier = object : WristIdentifier {
            override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<String> {
                return if (chain == wrist || chain == wrist2) {
                    Option.empty()
                } else {
                    Option.just("")
                }
            }

            override fun isSphericalWrist(
                chain: ImmutableList<DhParam>,
                priorChain: ImmutableList<DhParam>,
                inverseTipTransform: FrameTransformation
            ): Either<String, ImmutableList<DhParam>> =
                fail { "Tried to call wrong isSphericalWrist method." }
        }

        val identifier = DefaultChainIdentifier(mockWristIdentifier)

        assertEquals(
            immutableListOf(
                SphericalWrist(wrist),
                RevoluteJoint(pin),
                SphericalWrist(wrist2)
            ),
            identifier.identifyChain(chain)
        )
    }
}
