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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Servo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus

/**
 * Generates [Servo] CAD.
 */
class ServoGenerator(
    shaftGenerator: VitaminCadGenerator<Shaft>,
    maxCacheSize: Long = 100
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
            val boltHoles = getBoltHoles(it, it.boltHoleDiameter, it.flangeHeight)

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

    /**
     * Generates a servo with bolt hole cylinders that can be used to cut out holes for the
     * bolts.
     *
     * @param vitamin The vitamin.
     * @param boltHoleDiameter The diameter of the bolt hole cylinders.
     * @param boltHoleLength The length of the bolt hole cylinders.
     */
    fun generateCAD(vitamin: Servo, boltHoleDiameter: Length, boltHoleLength: Length): CSG {
        val boltHoles = getBoltHoles(vitamin, boltHoleDiameter, boltHoleLength)
        return cache[vitamin].union(
            boltHoles.toZMin()
                .toYMin()
                .movey(-vitamin.boltDepthSeparation.millimeter + boltHoleDiameter.millimeter / 2)
                .movex(
                    -(vitamin.shaftCenterToTopOfBody + (vitamin.boltWidthSeparation - vitamin.width) / 2).millimeter
                )
                .movez(-(vitamin.height - vitamin.flangeHeightFromBottomOfBody - vitamin.flangeHeight).millimeter)
        )
    }

    private fun getBoltHoles(
        it: Servo,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG {
        val boltHole =
            Cylinder(boltHoleDiameter.millimeter / 2, boltHoleLength.millimeter).toCSG()
        return boltHole.union(
            boltHole.movex(it.boltWidthSeparation.millimeter),
            boltHole.movex(it.boltWidthSeparation.millimeter)
                .movey(it.boltDepthSeparation.millimeter),
            boltHole.movey(it.boltDepthSeparation.millimeter)
        )
    }
}
