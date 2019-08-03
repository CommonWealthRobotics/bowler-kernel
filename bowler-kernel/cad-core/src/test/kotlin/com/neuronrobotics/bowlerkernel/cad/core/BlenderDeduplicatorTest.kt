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
package com.neuronrobotics.bowlerkernel.cad.core

import eu.mihosoft.vrl.v3d.Cylinder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class BlenderDeduplicatorTest {

    private val deduplicator = BlenderDeduplicator()

    @Test
    fun `test dedup`() {
        val csg = Cylinder(50.0, 50.0, 100).toCSG().let { csg ->
            csg.union((0 until 20).map { csg.rotz(it) })
        }

        val csgOut = deduplicator.deduplicate(csg)

        assertAll(
            { assertEquals(csg.bounds.center, csgOut.bounds.center) },
            { assertEquals(csg.bounds.bounds, csgOut.bounds.bounds) }
        )
    }
}
