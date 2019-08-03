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

import Jama.Matrix
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class FrameTransformationMapperTest {

    @Test
    fun `test rotation and translation affine`() {
        val ft = FrameTransformation.fromMatrix(Matrix(4, 4).apply {
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    this[row, col] = row * 2.0 + col
                }
            }
        })

        val affine = ft.affine()

        assertAll(
            { assertEquals(0.0, affine.mxx) },
            { assertEquals(1.0, affine.mxy) },
            { assertEquals(2.0, affine.mxz) },
            { assertEquals(2.0, affine.myx) },
            { assertEquals(3.0, affine.myy) },
            { assertEquals(4.0, affine.myz) },
            { assertEquals(4.0, affine.mzx) },
            { assertEquals(5.0, affine.mzy) },
            { assertEquals(6.0, affine.mzz) },
            { assertEquals(3.0, affine.tx) },
            { assertEquals(5.0, affine.ty) },
            { assertEquals(7.0, affine.tz) }
        )
    }
}
