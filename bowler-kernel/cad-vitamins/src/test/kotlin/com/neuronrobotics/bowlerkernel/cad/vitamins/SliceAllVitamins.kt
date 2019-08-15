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
package com.neuronrobotics.bowlerkernel.cad.vitamins

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexEDRMotor
import eu.mihosoft.vrl.v3d.CSG
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class SliceAllVitamins {

    private val slicer = VitaminSlicer()

    @Test
    @Disabled
    fun `slice all vitamins`() {
        val vitamins: List<CSG> = VexMotorGenerator(ShaftGenerator()).let {
            listOf(
                it.generateCAD(VexEDRMotor.VexMotor269),
                it.generateCAD(VexEDRMotor.VexMotor393)
            )
        }

        val coms = vitamins.map {
            it to slicer.getCenterOfMass(it, sliceStep = 0.5)
        }

        println(coms.joinToString("\n"))
    }
}
