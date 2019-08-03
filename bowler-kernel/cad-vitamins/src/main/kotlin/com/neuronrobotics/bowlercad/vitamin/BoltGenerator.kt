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
package com.neuronrobotics.bowlercad.vitamin

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Bolt
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.millimeter

class BoltGenerator(
    numSlices: Int = 16,
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
}
