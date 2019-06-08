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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.BallBearing
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.millimeter

/**
 * Generates [BallBearing] CAD.
 */
class BallBearingGenerator(
    numSlices: Int = 16,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<BallBearing> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<BallBearing, CSG> {
            it!!

            val bearingOutside = Cylinder(
                it.diameter.millimeter / 2,
                it.width.millimeter,
                numSlices
            ).toCSG()

            val bore = Cylinder(
                it.bore.millimeter / 2,
                it.width.millimeter,
                numSlices
            ).toCSG()

            bearingOutside.difference(bore)
        })

    override fun generateCAD(vitamin: BallBearing): CSG = cache[vitamin]
}
