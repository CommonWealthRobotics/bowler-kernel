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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon

import com.beust.klaxon.TypeFor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Shaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.ShaftTypeAdapter

data class KlaxonShaft(
    @TypeFor(field = "shaft", adapter = ShaftTypeAdapter::class)
    val shaftType: Int,
    val shaft: Shaft
) : Shaft by shaft, KlaxonVitaminTo {

    override fun toVitamin() = shaft

    companion object : KlaxonVitaminFrom<Shaft> {

        override fun fromVitamin(other: Shaft) = KlaxonShaft(
            ShaftTypeAdapter().typeFor(other::class),
            other
        )
    }
}
