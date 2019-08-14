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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin

import com.beust.klaxon.TypeFor
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.ShaftTypeAdapter
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Power
import org.octogonapus.ktunits.quantities.Torque
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.lbFIn
import org.octogonapus.ktunits.quantities.lbM
import org.octogonapus.ktunits.quantities.millimeter
import org.octogonapus.ktunits.quantities.nM
import org.octogonapus.ktunits.quantities.revolutionPerMinute
import org.octogonapus.ktunits.quantities.volt

/**
 * A Vex EDR motor.
 *
 * TODO: Find the real power and CoM
 *
 * @param width The width of the body (between the face the wire attaches on and the opposite face).
 * @param depth The depth of the body (between the two large rectangular sides).
 * @param height The height of the body (between the face the shaft is on and the opposite face).
 * @param shaft The shaft type.
 * @param axelInset The shorter distance along the width from the edge to the axle.
 * @param postInset The shorter distance along the width from the edge to the post.
 * @param postDiameterBottom The diameter of one post at the bottom.
 * @param postDiameterTop The diameter of one post at the top.
 * @param voltage The operating voltage.
 * @param freeSpeed The free speed.
 * @param freeCurrent The free current.
 * @param stallTorque The stall torque.
 * @param stallCurrent The stall current.
 * @param power The maximum power output.
 */
sealed class VexEDRMotor(
    val width: Length,
    val depth: Length,
    val height: Length,
    @TypeFor(field = "shaft", adapter = ShaftTypeAdapter::class)
    val shaftType: Int,
    val shaft: Shaft,
    val axelInset: Length,
    val postInset: Length,
    val postDiameterBottom: Length,
    val postDiameterTop: Length,
    val voltage: ElectricPotential,
    val freeSpeed: AngularVelocity,
    val freeCurrent: ElectricCurrent,
    val stallTorque: Torque,
    val stallCurrent: ElectricCurrent,
    val power: Power,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : Vitamin, KlaxonVitaminTo {

    object VexMotor393 : VexEDRMotor(
        width = 50.2.millimeter,
        depth = 25.3.millimeter,
        height = 41.millimeter,
        shaftType = ShaftTypeAdapter().typeFor(DefaultShaft.SquareShaft::class),
        shaft = DefaultShaft.SquareShaft(
            width = (1.0 / 8.0).inch,
            height = 2.inch,
            mass = 20.gram,
            specs = emptyImmutableMap()
        ),
        axelInset = 7.millimeter,
        postInset = 8.4.millimeter,
        postDiameterBottom = 8.9.millimeter,
        postDiameterTop = 8.3.millimeter,
        voltage = 7.2.volt,
        freeSpeed = 100.revolutionPerMinute,
        freeCurrent = 0.37.ampere,
        stallTorque = 1.67.nM,
        stallCurrent = 4.8.ampere,
        power = TODO(),
        mass = 0.192.lbM,
        centerOfMass = TODO(),
        specs = emptyImmutableMap()
    ) {
        override fun toVitamin() = this
    }

    object VexMotor269 : VexEDRMotor(
        width = 40.millimeter,
        depth = 20.millimeter,
        height = 35.5.millimeter,
        shaftType = ShaftTypeAdapter().typeFor(DefaultShaft.SquareShaft::class),
        shaft = DefaultShaft.SquareShaft(
            width = (1.0 / 8.0).inch,
            height = 2.inch,
            mass = 20.gram,
            specs = emptyImmutableMap()
        ),
        axelInset = 4.5.millimeter,
        postInset = 1.millimeter,
        postDiameterBottom = 7.8.millimeter,
        postDiameterTop = 7.2.millimeter,
        voltage = 7.2.volt,
        freeSpeed = 100.revolutionPerMinute,
        freeCurrent = 0.18.ampere,
        stallTorque = 8.6.lbFIn,
        stallCurrent = 2.6.ampere,
        power = TODO(),
        mass = 0.134.lbM,
        centerOfMass = TODO(),
        specs = emptyImmutableMap()
    ) {
        override fun toVitamin() = this
    }
}
