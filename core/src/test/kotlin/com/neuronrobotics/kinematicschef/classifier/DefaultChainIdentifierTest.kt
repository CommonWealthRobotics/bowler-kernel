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
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.not
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.ejml.simple.SimpleMatrix
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.plus
import kotlin.test.assertEquals

internal class DefaultChainIdentifierTest {

    @Test
    fun `test single revolute joint`() {
        val chain = TestUtil.randomDhParamList(1)

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(chain) } doReturn Option.just(ClassifierError(""))
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
            on { isSphericalWrist(chain) } doReturn Option.just(ClassifierError(""))
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
            on { isSphericalWrist(not(wrist)) } doReturn Option.just(ClassifierError(""))
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
            on { isSphericalWrist(not(wrist)) } doReturn Option.just(ClassifierError(""))
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
            on { isSphericalWrist(not(wrist)) } doReturn Option.just(ClassifierError(""))
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
            override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<ClassifierError> {
                return if (chain == wrist || chain == wrist2) {
                    Option.empty()
                } else {
                    Option.just(ClassifierError(""))
                }
            }

            override fun isSphericalWrist(
                chain: ImmutableList<DhParam>,
                priorChain: ImmutableList<DhParam>,
                inverseTipTransform: SimpleMatrix
            ): Either<ClassifierError, ImmutableList<DhParam>> {
                TODO("not implemented")
            }
        }

        // argThat doesn't work currently
        /*
        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(or(wrist, wrist2)) } doReturn Option.empty()
            on {
                isSphericalWrist(argThat {
                    !(equals(wrist) && equals(wrist2))
                })
            } doReturn Option.just(ClassifierError(""))
        }
        */

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
