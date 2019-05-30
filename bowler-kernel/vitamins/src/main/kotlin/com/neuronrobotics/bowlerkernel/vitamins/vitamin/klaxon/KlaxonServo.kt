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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Servo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.ShaftTypeAdapter
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Torque

data class KlaxonServo(
    override val width: Length,
    override val length: Length,
    override val height: Length,
    override val shaftCenterToTopOfBody: Length,
    override val flangeHeight: Length,
    override val flangeWidth: Length,
    override val flangeThickness: Length,
    override val boltHeightSeparation: Length,
    override val boltWidthSeparation: Length,
    override val voltage: ElectricPotential,
    override val stallTorque: Torque,
    override val speed: AngularVelocity,
    @TypeFor(field = "shaft", adapter = ShaftTypeAdapter::class)
    val shaftType: Int,
    override val shaft: DefaultShaft,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : Servo, KlaxonVitaminTo {

    override fun toVitamin() = DefaultServo(
        width = width,
        length = length,
        height = height,
        shaftCenterToTopOfBody = shaftCenterToTopOfBody,
        flangeHeight = flangeHeight,
        flangeWidth = flangeWidth,
        flangeThickness = flangeThickness,
        boltHeightSeparation = boltHeightSeparation,
        boltWidthSeparation = boltWidthSeparation,
        voltage = voltage,
        stallTorque = stallTorque,
        speed = speed,
        shaft = shaft,
        mass = mass,
        centerOfMass = centerOfMass,
        specs = specs
    )

    companion object : KlaxonVitaminFrom<Servo> {

        override fun fromVitamin(other: Servo) = KlaxonServo(
            width = other.width,
            length = other.length,
            height = other.height,
            shaftCenterToTopOfBody = other.shaftCenterToTopOfBody,
            flangeHeight = other.flangeHeight,
            flangeWidth = other.flangeWidth,
            flangeThickness = other.flangeThickness,
            boltHeightSeparation = other.boltHeightSeparation,
            boltWidthSeparation = other.boltWidthSeparation,
            voltage = other.voltage,
            stallTorque = other.stallTorque,
            speed = other.speed,
            shaftType = ShaftTypeAdapter().typeFor(other.shaft::class),
            shaft = other.shaft,
            mass = other.mass,
            centerOfMass = other.centerOfMass,
            specs = other.specs
        )
    }
}
