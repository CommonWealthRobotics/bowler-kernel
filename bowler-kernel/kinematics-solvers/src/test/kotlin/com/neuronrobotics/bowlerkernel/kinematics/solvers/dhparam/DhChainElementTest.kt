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
package com.neuronrobotics.bowlerkernel.kinematics.solvers.dhparam

import com.neuronrobotics.bowlerkernel.kinematics.solvers.TestUtil
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
