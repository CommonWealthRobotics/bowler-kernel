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

import org.octogonapus.ktunits.quantities.Length

/**
 * A generic ball bearing.
 *
 * For CAD, x=0, y=0 should be in the center of the bore. z=0 should be on one of the two flat
 * faces of the bearing such that the z-axis is aligned with the bore axis and the bearing
 * extends vertically into the positive z range.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Construction type
 *  - Lubrication
 */
interface BallBearing : Vitamin {

    /**
     * The thickness between the two flat faces.
     */
    val width: Length

    /**
     * The outer diameter.
     */
    val diameter: Length

    /**
     * The bore diameter (inner diameter).
     */
    val bore: Length
}
