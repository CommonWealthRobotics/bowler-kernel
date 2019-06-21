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
import org.octogonapus.ktunits.quantities.Mass

/**
 * VEX brand angle (l-channel).
 *
 * @param type The type of the metal.
 * @param width The width in holes (typically 2 or 3).
 * @param height The height in holes (typically equal to [width]).
 * @param length The length in holes (typically 25 or 35).
 */
data class VexAngle(
    val type: VexMetal,
    val width: Int,
    val height: Int,
    val length: Int,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : Vitamin, KlaxonVitaminTo {

    constructor(
        type: VexMetal,
        width: Int,
        length: Int,
        mass: Mass,
        centerOfMass: CenterOfMass,
        specs: ImmutableMap<String, Any>
    ) : this(type, width, width, length, mass, centerOfMass, specs)

    override fun toVitamin() = this
}
