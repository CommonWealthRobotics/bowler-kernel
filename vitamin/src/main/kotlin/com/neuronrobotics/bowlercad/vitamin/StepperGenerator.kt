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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Bolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.StepperMotor
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.millimeter

class StepperGenerator(
    boltGenerator: VitaminCadGenerator<Bolt>,
    shaftGenerator: VitaminCadGenerator<Shaft>,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<StepperMotor> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<StepperMotor, CSG> {
            it!!

            val body = Cube(it.width.millimeter, it.width.millimeter, it.height.millimeter)
                .toCSG()
                .toZMax()

            val bolt = boltGenerator.generateCAD(it.bolt).toZMax()
            val bolts = CSG.unionAll(
                mutableListOf(
                    bolt.movex(it.boltHoleSpacing.millimeter / 2)
                        .movey(it.boltHoleSpacing.millimeter / 2),
                    bolt.movex(-it.boltHoleSpacing.millimeter / 2)
                        .movey(it.boltHoleSpacing.millimeter / 2),
                    bolt.movex(it.boltHoleSpacing.millimeter / 2)
                        .movey(-it.boltHoleSpacing.millimeter / 2),
                    bolt.movex(-it.boltHoleSpacing.millimeter / 2)
                        .movey(-it.boltHoleSpacing.millimeter / 2)
                )
            )

            val shaft = shaftGenerator.generateCAD(it.shaft).toZMin()

            body.difference(bolts).union(shaft)
        })

    override fun generateCAD(vitamin: StepperMotor): CSG = cache[vitamin]

    /**
     * Generates a stepper motor with bolt hole cylinders that can be used to cut out holes for the
     * bolts.
     *
     * @param vitamin The vitamin.
     * @param boltHoleDiameter The diameter of the bolt hole cylinders.
     * @param boltHoleLength The length of the bolt hole cylinders.
     */
    fun generateCAD(
        vitamin: StepperMotor,
        boltHoleDiameter: Length,
        boltHoleLength: Length
    ): CSG = cache[vitamin]
}
