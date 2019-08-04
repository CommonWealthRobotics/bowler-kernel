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

import eu.mihosoft.vrl.v3d.CSG
import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.millimeter
import kotlin.math.truncate

/**
 * Unions a [bolt] [CSG] in a circle of a given [diameter], starting at angle [angleOffset] and
 * moving in increments of [angleIncrement] until completing the full circle.
 *
 * @param diameter The diameter of the bolt circle.
 * @param angleOffset The angle, offset from zero degrees, of the first bolt.
 * @param angleIncrement The angle of each bolt relative to the previous.
 * @param bolt The bolt [CSG].
 * @return A union of all bolts in the circle.
 */
fun getBoltCircle(diameter: Length, angleOffset: Angle, angleIncrement: Angle, bolt: CSG): CSG {
    val startingBolt = bolt.toZMax()
        .movex(diameter.millimeter / 2)
        .rotz(angleOffset.degree)

    val numberOfBolts = truncate(360 / angleIncrement.degree).toInt()
    val allBolts = (1..numberOfBolts).map { i ->
        startingBolt.rotz(angleIncrement.degree * i)
    }

    return CSG.unionAll(allBolts).toZMax()
}
