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

import org.octogonapus.ktunits.quantities.Angle
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Stiffness

/**
 * A coiled torsion spring.
 */
interface TorsionSpring : Vitamin {

    /**
     * The undeflected length.
     */
    val freeLength: Length

    /**
     * The undeflected angle.
     */
    val freeAngle: Angle

    /**
     * The outer diameter of the coils.
     */
    val outerDiameter: Length

    /**
     * The diameter of the wire at the coils.
     */
    val wireDiameter: Length

    /**
     * The length of a leg.
     */
    val legLength: Length

    /**
     * The spring constant.
     */
    val springConstant: Stiffness

    /**
     * The maximum load.
     */
    val maximumLoad: Mass
}
