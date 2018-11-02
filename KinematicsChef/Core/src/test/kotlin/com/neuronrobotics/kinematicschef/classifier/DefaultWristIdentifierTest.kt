/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.util.immutableListOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DefaultWristIdentifierTest {

    private val identifier = DefaultWristIdentifier()

    @Test
    fun `test a chain less than three links`() {
        assertTrue(
            identifier.isSphericalWrist(
                immutableListOf(DhParam.zero, DhParam.zero)
            ).nonEmpty()
        )
    }

    @Test
    fun `test a chain more than three links`() {
        assertTrue(
            identifier.isSphericalWrist(
                immutableListOf(DhParam.zero, DhParam.zero, DhParam.zero, DhParam.zero)
            ).nonEmpty()
        )
    }
}
