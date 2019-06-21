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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.StepperMotor
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Torque

data class KlaxonStepperMotor(
    override val width: Length,
    override val height: Length,
    override val boltHoleSpacing: Length,
    override val bolt: DefaultBolt,
    @TypeFor(field = "shaft", adapter = ShaftTypeAdapter::class)
    val shaftType: Int,
    override val shaft: DefaultShaft,
    override val voltage: ElectricPotential,
    override val holdingTorque: Torque,
    override val current: ElectricCurrent,
    override val stepAngle: Angle,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : StepperMotor, KlaxonVitaminTo {

    override fun toVitamin() =
        DefaultStepperMotor(
            width = width,
            height = height,
            boltHoleSpacing = boltHoleSpacing,
            bolt = bolt,
            shaft = shaft,
            voltage = voltage,
            holdingTorque = holdingTorque,
            current = current,
            stepAngle = stepAngle,
            mass = mass,
            centerOfMass = centerOfMass,
            specs = specs
        )

    companion object : KlaxonVitaminFrom<StepperMotor> {

        override fun fromVitamin(other: StepperMotor) = KlaxonStepperMotor(
            width = other.width,
            height = other.height,
            boltHoleSpacing = other.boltHoleSpacing,
            bolt = other.bolt,
            shaftType = ShaftTypeAdapter().typeFor(other.shaft::class),
            voltage = other.voltage,
            holdingTorque = other.holdingTorque,
            current = other.current,
            stepAngle = other.stepAngle,
            shaft = other.shaft,
            mass = other.mass,
            centerOfMass = other.centerOfMass,
            specs = other.specs
        )
    }
}
