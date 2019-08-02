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

import com.google.common.math.DoubleMath
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.meter

data class CenterOfMass(
    val x: Length,
    val y: Length,
    val z: Length
) {

    /**
     * Whether this instance approximately equals the [other] instance within the per-dimension
     * [tolerance].
     *
     * @param other The other instance.
     * @param tolerance The per-element equality tolerance.
     * @param units The unit to convert to before checking equality.
     * @return True if this instance is approximately equal to the [other] instance.
     */
    fun approxEquals(
        other: CenterOfMass,
        tolerance: Double = 1e-10,
        units: Length.() -> Double = Length::meter
    ) = DoubleMath.fuzzyEquals(x.units(), other.x.units(), tolerance) &&
        DoubleMath.fuzzyEquals(y.units(), other.y.units(), tolerance) &&
        DoubleMath.fuzzyEquals(z.units(), other.z.units(), tolerance)
}
