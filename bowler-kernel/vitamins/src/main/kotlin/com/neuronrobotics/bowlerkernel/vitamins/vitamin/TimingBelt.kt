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

interface TimingBelt : Vitamin {

    /**
     * The total height of the belt.
     */
    val height: Length

    /**
     * The width of the belt.
     */
    val width: Length

    /**
     * The thickness of a tooth (not including the section of belt it is on top of).
     */
    val toothHeight: Length

    /**
     * The distance between teeth at the base.
     */
    val pitchWidth: Length
}
