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
 * A shaft for transferring torque.
 *
 * For CAD, x=0, y=0 should be in the center of the shaft. z=0 should be on one of the smaller
 * flat faces of the shaft such that the z-axis is aligned with the shaft and the shaft extends
 * vertically into the positive z range.
 */
interface Shaft : Vitamin {

    /**
     * The overall width (x-axis), not including keep-away zones for CAD.
     */
    val width: Length

    /**
     * The overall length (y-axis), not including keep-away zones for CAD.
     */
    val length: Length

    /**
     * The overall height (z-axis), not including keep-away zones for CAD.
     */
    val height: Length
}
