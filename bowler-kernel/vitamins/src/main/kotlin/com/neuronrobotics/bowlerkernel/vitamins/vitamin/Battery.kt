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

import org.octogonapus.ktunits.quantities.ElectricCharge
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Length

/**
 * A generic battery.
 *
 * For CAD, x=0, y=0, z=0 should be on the bottom, upper-left corner of the battery.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Number of cells
 *  - Connector type
 *  - Chemistry
 */
interface Battery : Vitamin {

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

    /**
     * The nominal voltage.
     */
    val voltage: ElectricPotential

    /**
     * The maximum continuous current draw.
     */
    val current: ElectricCurrent

    /**
     * The capacity.
     */
    val capacity: ElectricCharge
}
