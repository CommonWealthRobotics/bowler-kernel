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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.StepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.annotation.max
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.plus

class StepperGenerator(
    boltGenerator: VitaminCadGenerator<Bolt>,
    shaftGenerator: VitaminCadGenerator<Shaft>,
    private val numSlices: Int = 16,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<StepperMotor> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<StepperMotor, CSG> {
            it!!

            val body = Cube(it.width.millimeter, it.width.millimeter, it.height.millimeter)
                .toCSG()
                .toZMax()

            val bolts = getBolts(it, boltGenerator.generateCAD(it.bolt))

            val shaft = shaftGenerator.generateCAD(it.shaft).toZMin()

            body.difference(bolts).union(shaft)
        })

    override fun generateCAD(vitamin: StepperMotor): CSG = cache[vitamin]

    override fun generateKeepaway(vitamin: StepperMotor): CSG =
        generateKeepaway(
            vitamin = vitamin,
            boltHoleDiameter = max(vitamin.bolt.headDiameter, vitamin.bolt.throughHoleDiameter),
            boltHoleLength = vitamin.bolt.headHeight + vitamin.bolt.bodyHeight
        )

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
        vitamin: StepperMotor,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG = cache[vitamin].union(generateBolts(vitamin, boltHoleDiameter, boltHoleLength))

    /**
     * Generates the bolts for a stepper motor that can be used to cut out holes for the bolts.
     *
     * @param vitamin The vitamin.
     * @param boltHoleDiameter The diameter of the bolt hole cylinders.
     * @param boltHoleLength The length of the bolt hole cylinders.
     */
    fun generateBolts(
        vitamin: StepperMotor,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG {
        val startingBolt = Cylinder(
            boltHoleDiameter.millimeter / 2,
            boltHoleLength.millimeter,
            numSlices
        ).toCSG()

        return getBolts(vitamin, startingBolt).toZMin()
    }

    private fun getBolts(motor: StepperMotor, bolt: CSG): CSG {
        val startingBolt = bolt.toZMax()

        return CSG.unionAll(
            mutableListOf(
                startingBolt.movex(motor.boltHoleSpacing.millimeter / 2)
                    .movey(motor.boltHoleSpacing.millimeter / 2),
                startingBolt.movex(-motor.boltHoleSpacing.millimeter / 2)
                    .movey(motor.boltHoleSpacing.millimeter / 2),
                startingBolt.movex(motor.boltHoleSpacing.millimeter / 2)
                    .movey(-motor.boltHoleSpacing.millimeter / 2),
                startingBolt.movex(-motor.boltHoleSpacing.millimeter / 2)
                    .movey(-motor.boltHoleSpacing.millimeter / 2)
            )
        )
    }
}