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
package com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier.gitvitaminsupplier

import com.beust.klaxon.TypeFor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo

data class KlaxonGitVitamin(
    /**
     * The type of this vitamin, used by Klaxon to handle polymorphism.
     */
    @TypeFor(field = "vitamin", adapter = KlaxonVitaminAdapter::class)
    val type: String,

    /**
     * The vitamin.
     */
    val vitamin: KlaxonVitaminTo,

    /**
     * The part number.
     */
    val partNumber: String,

    /**
     * The price for one unit.
     */
    val price: Double
) {

    companion object {

        fun from(other: KlaxonVitaminTo, partNumber: String, price: Double) =
            KlaxonGitVitamin(
                type = other::class.simpleName!!,
                vitamin = other,
                partNumber = partNumber,
                price = price
            )
    }
}
