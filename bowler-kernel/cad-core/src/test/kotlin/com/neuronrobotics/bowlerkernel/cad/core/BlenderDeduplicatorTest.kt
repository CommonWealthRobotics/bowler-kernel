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

import arrow.core.Either
import eu.mihosoft.vrl.v3d.Cylinder
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class BlenderDeduplicatorTest {

    private val deduplicator = BlenderDeduplicator()

    private val csg = Cylinder(50.0, 50.0, 100).toCSG().let { csg ->
        csg.union((0 until 20).map { csg.rotz(it) })
    }

    @Test
    @Tag("needsSpecialSoftware")
    fun `test dedup`() {
        val csgOut = deduplicator.deduplicate(csg).attempt().unsafeRunSync()

        assertTrue(csgOut is Either.Right)
        csgOut as Either.Right

        assertAll(
            { assertEquals(csg.bounds.center, csgOut.b.bounds.center) },
            { assertEquals(csg.bounds.bounds, csgOut.b.bounds.bounds) }
        )
    }

    @Test
    fun `test with invalid blender executable`() {
        val result = BlenderDeduplicator(blenderExec = "NotABlenderExec")
            .deduplicate(csg)
            .attempt()
            .unsafeRunSync()

        assertTrue(result is Either.Left)
        result as Either.Left
        assertTrue(result.a is IOException)
    }
}
