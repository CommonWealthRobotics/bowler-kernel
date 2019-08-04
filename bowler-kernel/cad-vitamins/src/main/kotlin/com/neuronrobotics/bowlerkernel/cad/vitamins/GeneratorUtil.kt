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
