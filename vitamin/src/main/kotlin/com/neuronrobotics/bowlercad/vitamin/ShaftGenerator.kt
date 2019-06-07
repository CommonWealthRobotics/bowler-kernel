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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus

class ShaftGenerator(
    maxCacheSize: Long = 100
) : VitaminCadGenerator<Shaft> {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize)
        .build(CacheLoader.from<Shaft, CSG> {
            it!!

            when (it) {
                is DefaultShaft -> when (it) {
                    is DefaultShaft.SquareShaft -> makeSquareShaft(it)

                    is DefaultShaft.ServoHorn -> when (it) {
                        is DefaultShaft.ServoHorn.Arm -> makeArm(it)
                        is DefaultShaft.ServoHorn.Wheel -> makeWheel(it)
                    }

                    else -> throw IllegalArgumentException()
                }

                else -> throw IllegalArgumentException()
            }
        })

    private fun makeSquareShaft(shaft: DefaultShaft.SquareShaft): CSG =
        Cube(shaft.length.millimeter, shaft.length.millimeter, shaft.height.millimeter).toCSG().toZMin()

    private fun makeArm(arm: DefaultShaft.ServoHorn.Arm): CSG {
        val base = Cylinder(arm.baseDiameter.millimeter / 2, arm.thickness.millimeter).toCSG()
        val tip = Cylinder(arm.tipDiameter.millimeter / 2, arm.thickness.millimeter).toCSG()
        val baseColumn =
            Cylinder(arm.baseDiameter.millimeter / 2, arm.baseColumnThickness.millimeter).toCSG()
        val armCSG = baseColumn.toZMin().union(
            base.union(tip.movex(arm.baseCenterToTipCenterLength.millimeter))
                .hull()
                .movez((arm.baseColumnThickness - arm.thickness).millimeter)
        )
        return makeCompoundArm(armCSG, arm.points)
    }

    private fun makeCompoundArm(armCSG: CSG, arms: Int): CSG {
        val armAngleDelta = 360 / arms
        return (0..arms).map {
            armCSG.rotz(armAngleDelta * it)
        }.let { CSG.unionAll(it) }
    }

    private fun makeWheel(wheel: DefaultShaft.ServoHorn.Wheel): CSG {
        val base = Cylinder(wheel.diameter.millimeter / 2, wheel.thickness.millimeter).toCSG()
        val baseColumn = Cylinder(
            wheel.baseDiameter.millimeter / 2,
            wheel.baseColumnThickness.millimeter
        ).toCSG()
        return baseColumn.toZMin()
            .union(base.movez((wheel.baseColumnThickness - wheel.thickness).millimeter))
    }

    override fun generateCAD(vitamin: Shaft): CSG = cache[vitamin]
}
