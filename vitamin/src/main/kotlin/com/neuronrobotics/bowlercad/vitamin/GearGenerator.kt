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

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.minus
import org.octogonapus.ktunits.quantities.plus
import org.octogonapus.ktunits.quantities.radian
import org.octogonapus.ktunits.quantities.times
import java.lang.Math.cos
import java.lang.Math.toDegrees
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class GearGenerator {

    @SuppressWarnings("LongParameterList")
    fun makeGear(
        numTeeth: Int,
        thicknessIn: Length,
        bevelAngle: Angle,
        toothBaseArchLen: Length,
        face: Length,
        helical: Length
    ): List<Any> {
        val toothAngle = 360.degree / numTeeth
        val baseThickness = toothBaseArchLen / PI
        val thickness = thicknessIn - baseThickness
        val baseDiam = toothBaseArchLen * numTeeth / PI
        val topDiamOffset = thickness / tan(bevelAngle.radian)
        val topDiam = baseDiam - topDiamOffset * 2
        val baseDiamLower = baseDiam - baseThickness * 2 * cos(bevelAngle.radian)
        val totalThickness = thickness + baseThickness
        val toothDepth = baseThickness * 1.5

        val toothCutter = Cube(
            toothBaseArchLen.millimeter,
            toothBaseArchLen.millimeter,
            (face + thickness * 2).millimeter
        ).toCSG()
            .toXMin()
            .toYMin()
            .rotz(45)
            .scaley(0.4)
            .toZMax()
            .movez(thickness.millimeter * 1.5)
            .rotx(if (bevelAngle.degree < 90) 0.0 else helical.millimeter)
            .movex(-toothDepth.millimeter)
            .roty(90 - bevelAngle.degree)
            .movez(totalThickness.millimeter)
            .movex(topDiam.millimeter / 2)
            .let {
                val angleScale = 0.2
                val cutterOffset = toothAngle * angleScale

                // Form a wedge shape
                it.rotz(-cutterOffset.degree).union(it.rotz(cutterOffset.degree)).hull()
            }

        val upperSection = Cylinder(
            baseDiam.millimeter / 2,
            topDiam.millimeter / 2,
            thickness.millimeter,
            numTeeth
        ).toCSG()

        val lowerSection = Cylinder(
            baseDiamLower.millimeter / 2,
            baseDiam.millimeter / 2,
            baseThickness.millimeter,
            numTeeth
        ).toCSG().toZMax()

        // Cut all the teeth
        var blank = upperSection.union(lowerSection).toZMin()
        for (i in 0 until numTeeth) {
            blank = blank.difference(toothCutter.rotz(toothAngle.degree * i))
        }

        val pinRadius = ((3 / 16).inch / 2 + 0.1.millimeter).millimeter
        val pinLength = 2.5.inch.millimeter
        val hole = Cylinder(pinRadius, pinRadius, pinLength, 30).toCSG().movez(-pinLength / 2)

        return listOf(
            blank.difference(hole).rotz(180),
            baseDiam / 2,
            toothAngle,
            toothDepth
        )
    }

    @SuppressWarnings("LongParameterList", "LongMethod")
    fun makeBevelBox(
        numDriveTeeth: Int,
        numDrivenTeeth: Int,
        thickness: Length,
        toothBaseArchLen: Length,
        axelAngleIn: Angle = 90.degree,
        helical: Length = 0.millimeter,
        meshInterferenceIn: Length? = null
    ): List<Any> {
        var axelAngle = axelAngleIn
        var meshInterference = meshInterferenceIn

        if (axelAngle.degree > 90) {
            axelAngle = 90.degree
        }
        if (axelAngle.degree < 0) {
            axelAngle = 0.degree
        }

        val baseThickness = toothBaseArchLen / PI
        val bevelTriangleAngle = PI.radian - axelAngle
        val lengthOfBevelCenter = sqrt(
            numDriveTeeth.pow(2) + numDrivenTeeth.pow(2) - 2 *
                numDrivenTeeth * numDriveTeeth * cos(bevelTriangleAngle.radian)
        )
        val kValue = numDrivenTeeth * numDriveTeeth * sin(bevelTriangleAngle.radian) / 2
        val height = 2 * kValue / lengthOfBevelCenter

        val bevelAngle = acos(height / numDrivenTeeth)
        val bevelAngleB = acos(height / numDriveTeeth)
        val face = (thickness - baseThickness) / sin(bevelAngle)
        val otherThick = face * sin(bevelAngleB) + baseThickness

        val gearA = makeGear(
            numDriveTeeth,
            thickness,
            toDegrees(bevelAngle).degree,
            toothBaseArchLen,
            face,
            helical
        )

        val gearB = makeGear(
            numDrivenTeeth,
            otherThick,
            toDegrees(bevelAngleB).degree,
            toothBaseArchLen,
            face,
            -1 * helical
        )

        if (meshInterference == null) {
            meshInterference = gearA[3] as Length * cos(axelAngle.radian) * 0.75 + 0.1.millimeter
        }

        val aDiam =
            gearB[1] as Length * cos(axelAngle.radian) + gearA[1] as Length - meshInterference
        val bDiam = gearB[1] as Length * sin(axelAngle.radian)
        val bangle = gearB[2] as Angle

        val gearBFinal = (gearB[0] as CSG)
            .rotz(bangle.degree / 2)
            .roty(axelAngle.degree)
            .movex(aDiam.millimeter)
            .movez(bDiam.millimeter)
            .rotz(180)
        val gearAFinal = gearA[0] as CSG
        val ratio =
            (gearA[1] as Length - meshInterference).millimeter / (gearB[1] as Length - meshInterference).millimeter

        return listOf(
            gearAFinal,
            gearBFinal,
            aDiam,
            bDiam,
            bevelAngle,
            face,
            otherThick,
            ratio,
            gearA[2],
            gearB[2]
        )
    }

    fun computeGearPitch(diameterAtCrown: Length, numberOfTeeth: Int): Length =
        (diameterAtCrown.millimeter / 2 * (360.0 / numberOfTeeth) * Math.PI / 180).millimeter
}

private fun Int.pow(i: Int): Double = toDouble().pow(i)
