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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin

import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.plus

sealed class DefaultShaft(
    override val width: Length,
    override val length: Length,
    override val height: Length,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : Shaft {

    /**
     * A shaft with a square profile.
     */
    data class SquareShaft(
        override val width: Length,
        override val height: Length,
        override val mass: Mass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, width, height, mass, CenterOfMass(0.inch, 0.inch, height / 2), specs)

    /**
     * A circular shaft.
     */
    data class RoundShaft(
        /**
         * The diameter of the shaft.
         */
        val diameter: Length,
        override val height: Length,
        override val mass: Mass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(
        diameter,
        diameter,
        height,
        mass,
        CenterOfMass(0.inch, 0.inch, height / 2),
        specs
    )

    /**
     * A circular shaft with a flat.
     */
    data class DShaft(
        /**
         * The diameter of the shaft.
         */
        val diameter: Length,

        /**
         * The distance from the flat to the opposite side of the shaft.
         */
        val flatToOppositeSide: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(diameter, diameter, height, mass, centerOfMass, specs)

    sealed class ServoHorn(
        override val width: Length,
        override val length: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, length, height, mass, centerOfMass, specs) {

        data class Arm(
            /**
             * The diameter of the base (where the arm attaches to the servo).
             */
            val baseDiameter: Length,

            /**
             * The diameter of the tip.
             */
            val tipDiameter: Length,

            /**
             * The center-to-center length between the base and tip.
             */
            val baseCenterToTipCenterLength: Length,

            /**
             * The thickness of the overall arm (excluding [baseColumnThickness].
             */
            val thickness: Length,

            /**
             * The thickness of the column at the base of the arm.
             */
            val baseColumnThickness: Length,

            /**
             * The number of points. Each point is another "single" arm.
             */
            val points: Int,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(
            width = baseCenterToTipCenterLength + baseDiameter / 2 + tipDiameter / 2,
            length = baseDiameter,
            height = baseColumnThickness,
            mass = mass,
            centerOfMass = centerOfMass,
            specs = specs
        )

        data class Wheel(
            /**
             * The diameter of the wheel.
             */
            val diameter: Length,

            /**
             * The diameter of the base (where the arm attaches to the servo).
             */
            val baseDiameter: Length,

            /**
             * The thickness of the overall wheel (excluding [baseColumnThickness]).
             */
            val thickness: Length,

            /**
             * The thickness of the column at the base of the arm.
             */
            val baseColumnThickness: Length,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(
            width = diameter,
            length = diameter,
            height = baseColumnThickness,
            mass = mass,
            centerOfMass = centerOfMass,
            specs = specs
        )
    }
}
