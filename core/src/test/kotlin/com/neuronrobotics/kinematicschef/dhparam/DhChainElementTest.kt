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
package com.neuronrobotics.kinematicschef.dhparam

import com.neuronrobotics.kinematicschef.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableListOf

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
