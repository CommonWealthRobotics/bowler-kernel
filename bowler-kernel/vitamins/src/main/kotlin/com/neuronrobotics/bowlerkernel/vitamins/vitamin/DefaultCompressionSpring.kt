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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass
import org.octogonapus.ktunits.quantities.Stiffness

data class DefaultCompressionSpring(
    override val uncompressedHeight: Length,
    override val outerDiameter: Length,
    override val wireDiameter: Length,
    override val springConstant: Stiffness,
    override val maximumLoad: Mass,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : CompressionSpring, KlaxonVitaminTo {

    override fun toVitamin() = this
}
