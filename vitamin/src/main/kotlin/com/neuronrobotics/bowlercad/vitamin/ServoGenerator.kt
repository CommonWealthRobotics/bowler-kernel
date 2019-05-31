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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Servo
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus

/**
 * Generates [Servo] CAD.
 */
class ServoGenerator(
    maxCacheSize: Long = 100,
    shaftGenerator: VitaminCadGenerator<DefaultShaft>
) : VitaminCadGenerator<Servo> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<Servo, CSG> {
            it!!

            val body = Cube(it.width.millimeter, it.depth.millimeter, it.height.millimeter).toCSG()
            val flange = Cube(
                it.flangeWidth.millimeter,
                it.flangeDepth.millimeter,
                it.flangeHeight.millimeter
            ).toCSG()

            val shaft = shaftGenerator.generateCAD(it.shaft)
            val boltHole =
                Cylinder(it.boltHoleDiameter.millimeter / 2, it.flangeHeight.millimeter).toCSG()
            val boltHoles = boltHole.union(
                boltHole.movex(it.boltWidthSeparation.millimeter),
                boltHole.movex(it.boltWidthSeparation.millimeter)
                    .movey(it.boltDepthSeparation.millimeter),
                boltHole.movey(it.boltDepthSeparation.millimeter)
            )

            val flangeWithHoles = flange.difference(boltHoles.move(boltHoles.center * -1.0))

            body.union(
                flangeWithHoles.movez(
                    body.maxZ + flange.totalZ / 2 -
                        (it.height - it.flangeHeightFromBottomOfBody).millimeter
                )
            ).movez(-body.maxZ)
                .movex(it.shaftCenterToTopOfBody.millimeter)
                .union(shaft)
        })

    override fun generateCAD(vitamin: Servo): CSG = cache[vitamin]
}
