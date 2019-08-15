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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexEDRMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter

class VexMotorGenerator(
    shaftGenerator: VitaminCadGenerator<Shaft>,
    numPostSlices: Int = 16,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<VexEDRMotor> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<VexEDRMotor, CSG> {
            it!!

            val motorBody = Cube(
                it.width.millimeter,
                it.depth.millimeter,
                it.height.millimeter
            ).toCSG()
                .toZMax()

            val posts = Cylinder(
                it.postDiameterBottom.millimeter / 2,
                it.postDiameterTop.millimeter / 2,
                14.8,
                numPostSlices
            ).toCSG().let { post ->
                post.union(post.movex(13))
            }.toZMin()
                .hull()

            val motorWithoutShaft = motorBody
                .toXMin()
                .movex(-it.axelInset.millimeter)
                .union(
                    posts.movex(
                        it.width.millimeter - it.postInset.millimeter -
                            it.postDiameterTop.millimeter * 2 - 13
                    )
                )

            val shaft = shaftGenerator.generateCAD(it.shaft)
                .toZMin()

            // TODO: Check these dimensions are accurate
            val shaftSupport = Cylinder(
                4.0,
                2.0,
                numPostSlices
            ).toCSG().toZMin()

            motorWithoutShaft.union(shaft)
                .union(shaftSupport)
        })

    override fun generateCAD(vitamin: VexEDRMotor): CSG = cache[vitamin]

    override fun generateKeepaway(vitamin: VexEDRMotor): CSG =
        // Use 6#32 cap screw head diameter
        generateKeepaway(vitamin, 6.5.millimeter, vitamin.shaft.height)

    /**
     * Generates the keepaway CAD for this [Vitamin]. This CAD can be used to perform a difference
     * operation to cut out a keepaway region in another CSG. This return value may be cached by
     * this generator.
     *
     * @param vitamin The [Vitamin].
     * @param boltHoleDiameter The diameter of the bolt hole cylinders.
     * @param boltHoleLength The length of the bolt hole cylinders.
     * @return The keepaway CAD for the [vitamin].
     */
    fun generateKeepaway(
        vitamin: VexEDRMotor,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG = cache[vitamin].union(generateBoltKeepaway(vitamin, boltHoleDiameter, boltHoleLength))

    /**
     * Generates the bolts for a dc motor that can be used to cut out holes for the bolts.
     *
     * @param vitamin The vitamin.
     * @param boltHoleDiameter The diameter of the bolt hole cylinders.
     * @param boltHoleLength The length of the bolt hole cylinders.
     */
    fun generateBoltKeepaway(
        vitamin: VexEDRMotor,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG {
        val bolt = Cylinder(
            boltHoleDiameter.millimeter / 2,
            boltHoleLength.millimeter
        ).toCSG()

        return bolt.union(bolt.movex(13))
            .movex(
                vitamin.width.millimeter - vitamin.postInset.millimeter -
                    vitamin.postDiameterTop.millimeter * 2 - 13
            )
            .toZMin()
            .movez(14.8)
    }
}
