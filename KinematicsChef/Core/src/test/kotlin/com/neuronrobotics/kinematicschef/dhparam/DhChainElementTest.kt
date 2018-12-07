/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.dhparam

import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.util.immutableListOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DhChainElementTest {

    @Test
    fun `test toDhParamList preserves order`() {
        val params = TestUtil.randomDhParamList(5)
        val elements = immutableListOf(
            RevoluteJoint(params.subList(0, 1)),
            SphericalWrist(params.subList(1, 4)),
            RevoluteJoint(params.subList(4, 5))
        )

        val paramsBack = elements.toDhParamList()
        assertEquals(params, paramsBack)
    }
}
