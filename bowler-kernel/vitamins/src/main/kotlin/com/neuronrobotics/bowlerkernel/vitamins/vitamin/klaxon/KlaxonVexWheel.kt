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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexWheelAdapter

data class KlaxonVexWheel(
    @TypeFor(field = "wheel", adapter = VexWheelAdapter::class)
    val wheelType: Int,
    val wheel: VexWheel
) : VexWheel by wheel, KlaxonVitaminTo {

    override fun toVitamin() = wheel

    companion object : KlaxonVitaminFrom<VexWheel> {

        override fun fromVitamin(other: VexWheel) = KlaxonVexWheel(
            VexWheelAdapter().typeFor(other::class),
            other
        )
    }
}
