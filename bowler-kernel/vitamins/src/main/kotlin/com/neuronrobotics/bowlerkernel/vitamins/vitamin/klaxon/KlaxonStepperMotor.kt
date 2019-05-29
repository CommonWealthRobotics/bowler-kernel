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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon

import com.beust.klaxon.TypeFor
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.ShaftTypeAdapter
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.StepperMotor
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Torque

data class KlaxonStepperMotor(
    override val nemaSize: Int,
    override val voltage: ElectricPotential,
    override val holdingTorque: Torque,
    override val current: ElectricCurrent,
    override val stepAngle: Angle,
    @TypeFor(field = "shaft", adapter = ShaftTypeAdapter::class)
    val shaftType: Int,
    override val shaft: DefaultShaft,
    override val width: Length,
    override val length: Length,
    override val height: Length,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : StepperMotor, KlaxonVitaminTo {

    override fun toVitamin() = DefaultStepperMotor(
        nemaSize = nemaSize,
        voltage = voltage,
        holdingTorque = holdingTorque,
        current = current,
        stepAngle = stepAngle,
        shaft = shaft,
        width = width,
        length = length,
        height = height,
        mass = mass,
        centerOfMass = centerOfMass,
        specs = specs
    )

    companion object : KlaxonVitaminFrom<StepperMotor> {

        override fun fromVitamin(other: StepperMotor) = KlaxonStepperMotor(
            nemaSize = other.nemaSize,
            voltage = other.voltage,
            holdingTorque = other.holdingTorque,
            current = other.current,
            stepAngle = other.stepAngle,
            shaftType = ShaftTypeAdapter().typeFor(other.shaft::class),
            shaft = other.shaft,
            width = other.width,
            length = other.length,
            height = other.height,
            mass = other.mass,
            centerOfMass = other.centerOfMass,
            specs = other.specs
        )
    }
}
