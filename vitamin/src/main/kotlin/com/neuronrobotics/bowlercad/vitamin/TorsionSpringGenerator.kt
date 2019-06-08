/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad.vitamin

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.TorsionSpring
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus

class TorsionSpringGenerator(
    numSlices: Int = 16,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<TorsionSpring> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<TorsionSpring, CSG> {
            it!!

            val core = Cylinder(
                it.outerDiameter.millimeter / 2,
                it.freeLength.millimeter,
                numSlices
            ).toCSG().difference(
                Cylinder(
                    (it.outerDiameter - it.wireDiameter).millimeter / 2,
                    it.freeLength.millimeter,
                    numSlices
                ).toCSG()
            )

            val leg = Cube(
                it.legLength.millimeter,
                it.wireDiameter.millimeter,
                it.wireDiameter.millimeter
            ).toCSG().toZMin().toXMin().toYMin().movey(-it.outerDiameter.millimeter / 2)

            core.union(leg).union(
                leg.toZMax()
                    .movey((it.outerDiameter - it.wireDiameter).millimeter)
                    .rotz(-it.freeAngle.degree)
                    .movez(it.freeLength.millimeter)
            )
        })

    override fun generateCAD(vitamin: TorsionSpring): CSG = cache[vitamin]
}
