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
 * A generic ball bearing. [Vitamin.width] should be the width (between the two flat faces of the
 * bearing). For rotational bearings, [Vitamin.height] and [Vitamin.length] should be equal to each
 * other and to the outer diameter. For linear bearings, [Vitamin.height] should be equal to the
 * outer diameter and [Vitamin.length] should be equal to either the outer diameter or the
 * outer housing dimension.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Construction type
 *  - Lubrication
 */
interface BallBearing : Vitamin {

    /**
     * The bore diameter (where the shaft goes).
     */
    val bore: Length
}
