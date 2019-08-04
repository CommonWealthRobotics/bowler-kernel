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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Nut
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.millimeter
import kotlin.math.sqrt

class NutGenerator(
    numPoints: Int = 6,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<Nut> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<Nut, CSG> {
            it!!

            val circumscribedRadius = it.width / sqrt(3.0)

            Cylinder(
                circumscribedRadius.millimeter,
                it.height.millimeter,
                numPoints
            ).toCSG().toZMin()
        })

    override fun generateCAD(vitamin: Nut): CSG = cache[vitamin]

    override fun generateKeepaway(vitamin: Nut): CSG = cache[vitamin]
}
