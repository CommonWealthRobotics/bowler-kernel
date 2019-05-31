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
                is DefaultShaft -> {
                    when (it) {
                        is DefaultShaft.ServoHorn -> {
                            when (it) {
                                is DefaultShaft.ServoHorn.Arm -> makeArm(it)

                                is DefaultShaft.ServoHorn.DoubleArm -> makeDoubleArm(it)

                                is DefaultShaft.ServoHorn.CrossArm -> {
                                    TODO()
                                }

                                is DefaultShaft.ServoHorn.Wheel -> {
                                    TODO()
                                }
                            }
                        }

                        else -> throw IllegalArgumentException()
                    }
                }

                else -> throw IllegalArgumentException()
            }
        })

    private fun makeArm(arm: DefaultShaft.ServoHorn.Arm): CSG {
        val base = Cylinder(arm.baseDiameter.millimeter / 2, arm.thickness.millimeter, 48).toCSG()
        val tip = Cylinder(arm.tipDiameter.millimeter / 2, arm.thickness.millimeter, 48).toCSG()
        val baseColumn =
            Cylinder(arm.baseDiameter.millimeter / 2, arm.baseColumnThickness.millimeter, 48).toCSG()
        return baseColumn.toZMin().union(
            base.union(tip.movex(arm.baseCenterToTipCenterLength.millimeter))
                .hull()
                .movez((arm.baseColumnThickness - arm.thickness).millimeter)
        )
    }

    private fun makeDoubleArm(arm: DefaultShaft.ServoHorn.DoubleArm): CSG {
        val singleArm = DefaultShaft.ServoHorn.Arm(
            baseDiameter = arm.baseDiameter,
            tipDiameter = arm.tipDiameter,
            baseCenterToTipCenterLength = arm.baseCenterToTipCenterLength,
            thickness = arm.thickness,
            baseColumnThickness = arm.baseColumnThickness,
            mass = arm.mass,
            centerOfMass = arm.centerOfMass,
            specs = arm.specs
        )

        return makeArm(singleArm).union(makeArm(singleArm).rotz(180))
    }

    override fun generateCAD(vitamin: Shaft): CSG = cache[vitamin]
}
