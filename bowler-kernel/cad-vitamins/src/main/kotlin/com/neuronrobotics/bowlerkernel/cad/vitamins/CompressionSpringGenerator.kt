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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CompressionSpring
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus

class CompressionSpringGenerator(
    numSlices: Int = 16,
    maxCacheSize: Long = 100
) : com.neuronrobotics.bowlerkernel.cad.vitamins.VitaminCadGenerator<CompressionSpring> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<CompressionSpring, CSG> {
            it!!

            val outerCylinder = Cylinder(
                it.outerDiameter.millimeter / 2,
                it.uncompressedHeight.millimeter,
                numSlices
            ).toCSG()

            val innerCylinder = Cylinder(
                (it.outerDiameter - it.wireDiameter).millimeter / 2,
                it.uncompressedHeight.millimeter,
                numSlices
            ).toCSG()

            outerCylinder.difference(innerCylinder)
        })

    override fun generateCAD(vitamin: CompressionSpring): CSG = cache[vitamin]
}
