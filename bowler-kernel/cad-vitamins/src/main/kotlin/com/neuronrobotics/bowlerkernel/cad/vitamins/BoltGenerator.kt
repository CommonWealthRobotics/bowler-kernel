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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Bolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.annotation.max
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.plus

class BoltGenerator(
    private val numSlices: Int = 16,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<Bolt> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<Bolt, CSG> {
            it!!

            val head = Cylinder(
                it.headDiameter.millimeter / 2,
                it.headHeight.millimeter,
                numSlices
            ).toCSG().toZMin()

            val body = Cylinder(
                it.throughHoleDiameter.millimeter / 2,
                it.bodyHeight.millimeter,
                numSlices
            ).toCSG().toZMax()

            body.union(head).toZMin()
        })

    override fun generateCAD(vitamin: Bolt): CSG = cache[vitamin]

    override fun generateKeepaway(vitamin: Bolt): CSG =
        generateKeepaway(vitamin, vitamin.headHeight + vitamin.bodyHeight)

    /**
     * Generates the keepaway CAD for this [Vitamin]. This CAD can be used to perform a difference
     * operation to cut out a keepaway region in another CSG. This return value may be cached by
     * this generator.
     *
     * @param vitamin The [Vitamin].
     * @param height The height of the keepaway cylinder.
     * @return The keepaway CAD for the [vitamin].
     */
    fun generateKeepaway(vitamin: Bolt, height: Length): CSG {
        val maxDiam = max(vitamin.headDiameter, vitamin.throughHoleDiameter)
        return Cylinder(
            maxDiam.millimeter / 2,
            height.millimeter / 2,
            numSlices
        ).toCSG().toZMin()
    }
}
