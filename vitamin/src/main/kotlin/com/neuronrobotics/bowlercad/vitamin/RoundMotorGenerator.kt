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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.RoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.millimeter
import kotlin.math.truncate

class RoundMotorGenerator(
    shaftGenerator: VitaminCadGenerator<Shaft>,
    boltGenerator: VitaminCadGenerator<Bolt>,
    maxCacheSize: Long = 100
) : VitaminCadGenerator<RoundMotor> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<RoundMotor, CSG> {
            it!!

            val encoder = Cylinder(
                it.encoderAssemblyDiameter.millimeter / 2,
                it.encoderAssemblyHeight.millimeter
            ).toCSG().toZMax()

            val motor = Cylinder(
                it.motorDiameter.millimeter / 2,
                it.motorHeight.millimeter
            ).toCSG().toZMax()

            val gearbox = Cylinder(
                it.gearboxDiameter.millimeter / 2,
                it.gearboxHeight.millimeter
            ).toCSG().toZMax()

            val shaftSupport = Cylinder(
                it.shaftSupportDiameter.millimeter / 2,
                it.shaftSupportHeight.millimeter
            ).toCSG().toZMin().movex(it.gearboxShaftOffset.millimeter)

            val shaft = shaftGenerator.generateCAD(it.shaft).toZMin()
                .movez(shaftSupport.totalZ)
                .movex(it.gearboxShaftOffset.millimeter)

            val bolts = getBolts(boltGenerator, it)

            gearbox.union(motor.movez(-gearbox.totalZ))
                .union(encoder.movez(-gearbox.totalZ - motor.totalZ))
                .union(shaftSupport)
                .union(shaft)
                .difference(bolts)
                .movex(-it.gearboxShaftOffset.millimeter)
        })

    private fun getBolts(boltGenerator: VitaminCadGenerator<Bolt>, motor: RoundMotor): CSG {
        val bolt = boltGenerator.generateCAD(motor.bolt)
            .movex(motor.boltCircleDiameter.millimeter / 2)
            .rotz(motor.boltCircleAngleOffset.degree)

        val numberOfBolts = truncate(360 / motor.boltCircleAngleIncrement.degree).toInt()
        val allBolts = (1..numberOfBolts).map { i ->
            bolt.rotz(motor.boltCircleAngleIncrement.degree * i)
        }

        return CSG.unionAll(allBolts).toZMax()
    }

    override fun generateCAD(vitamin: RoundMotor): CSG = cache[vitamin]
}
