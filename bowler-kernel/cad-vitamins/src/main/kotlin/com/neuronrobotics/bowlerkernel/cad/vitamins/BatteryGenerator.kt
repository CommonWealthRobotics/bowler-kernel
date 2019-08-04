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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Battery
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import org.octogonapus.ktunits.quantities.millimeter

class BatteryGenerator(
    maxCacheSize: Long = 100
) : VitaminCadGenerator<Battery> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<Battery, CSG> {
            it!!

            Cube(it.width.millimeter, it.length.millimeter, it.height.millimeter).toCSG()
                .toXMin()
                .toYMin()
                .toZMin()
        })

    override fun generateCAD(vitamin: Battery): CSG = cache[vitamin]

    override fun generateKeepaway(vitamin: Battery): CSG = cache[vitamin]
}
