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

import com.google.common.collect.ImmutableMap
import org.octogonapus.ktunits.quantities.Mass

/**
 * A Vitamin is typically something which cannot be 3d-printed; the electrical and hardware
 * components of a robot that you can buy from a store can typically be modeled as Vitamins.
 *
 * For aligning CAD, no origin constraints are enforced, but if any convention exists, it is
 * specified in the Vitamin's documentation.
 */
interface Vitamin {

    /**
     * The mass of this Vitamin.
     */
    val mass: Mass

    /**
     * The center of mass.
     */
    val centerOfMass: CenterOfMass

    /**
     * Any extra specifications that the human interacting with this Vitamin might want to know.
     */
    val specs: ImmutableMap<String, Any>
}
