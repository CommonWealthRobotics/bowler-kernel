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

import com.neuronrobotics.kinematicschef.TestUtil.randomDhParam
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.emptyImmutableList
import org.octogonapus.ktguava.collections.immutableListOf

internal class RevoluteJointTest {

    @Test
    fun `make revolute joint with 0 params`() {
        assertThrows<IllegalArgumentException> {
            RevoluteJoint(emptyImmutableList())
        }
    }

    @Test
    fun `make revolute joint with 1 param`() {
        RevoluteJoint(immutableListOf(randomDhParam()))
    }

    @Test
    fun `make revolute joint with 2 params`() {
        assertThrows<IllegalArgumentException> {
            RevoluteJoint(immutableListOf(randomDhParam(), randomDhParam()))
        }
    }
}
