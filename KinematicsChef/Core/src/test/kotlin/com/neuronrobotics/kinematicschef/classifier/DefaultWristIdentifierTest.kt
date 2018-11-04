/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.neuronrobotics.kinematicschef.TestUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DefaultWristIdentifierTest {

    private val identifier = DefaultWristIdentifier()

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test a chain less than three links`(chainSize: Int) {
        assertTrue(
            identifier.isSphericalWrist(
                TestUtil.randomDhParamList(chainSize)
            ).nonEmpty()
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 5, 6, 7])
    fun `test a chain more than three links`(chainSize: Int) {
        assertTrue(
            identifier.isSphericalWrist(
                TestUtil.randomDhParamList(chainSize)
            ).nonEmpty()
        )
    }
}
