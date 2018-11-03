/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Option
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.and
import com.neuronrobotics.kinematicschef.dhparam.DhChainElement
import com.neuronrobotics.kinematicschef.dhparam.RevoluteJoint
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.not
import com.neuronrobotics.kinematicschef.or
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.plus
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
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
    @Disabled
    fun `test spherical wrist, revolute joint, and a spherical wrist`() {
        val wrist = TestUtil.randomDhParamList(3)
        val pin = TestUtil.randomDhParamList(1)
        val wrist2 = TestUtil.randomDhParamList(3)
        val chain = wrist + pin + wrist2

        val mockWristIdentifier = mock<WristIdentifier> {
            on { isSphericalWrist(or(wrist, wrist2)) } doReturn Option.empty()
            on {
                isSphericalWrist(and(not(wrist), not(wrist2)))
            } doReturn Option.just(ClassifierError(""))
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
