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

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.TimingBelt
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus

class TimingBeltGenerator(
    private val defaultLength: Length = 40.millimeter
) : com.neuronrobotics.bowlerkernel.cad.vitamins.VitaminCadGenerator<TimingBelt> {

    override fun generateCAD(vitamin: TimingBelt): CSG = generateCAD(vitamin, defaultLength)

    fun generateCAD(vitamin: TimingBelt, length: Length): CSG {
        val toothLength = vitamin.pitchWidth / 3
        val toothStart = vitamin.height - vitamin.toothHeight
        val numTeeth = length.millimeter / vitamin.pitchWidth.millimeter

        val base = Cube(
            length.millimeter,
            vitamin.width.millimeter,
            toothStart.millimeter
        ).toCSG().toZMin().toXMin()

        val tooth = Cube(
            toothLength.millimeter,
            vitamin.width.millimeter,
            vitamin.toothHeight.millimeter
        ).toCSG().toXMin().toZMin().movez(toothStart.millimeter)

        val allTeeth = (0 until numTeeth.toInt()).map { toothNum ->
            tooth.movex(toothNum * vitamin.pitchWidth.millimeter)
        }

        return base.union(allTeeth)
    }
}
