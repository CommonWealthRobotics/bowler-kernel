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
package com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier

import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin

/**
 * A source of vitamins, typically an online store or inventorying API. The part numbers and
 * prices one instance returns are not necessarily transferable between different suppliers (i.e.,
 * each supplier is free to choose their own part numbers and prices as long as they are
 * internally consistent).
 */
interface VitaminSupplier {

    /**
     * The human-readable name of this supplier.
     */
    val name: String

    /**
     * All the vitamins this supplier can supply.
     */
    val allVitamins: ImmutableSet<Vitamin>

    /**
     * Queries the part number for a matching vitamin.
     *
     * @param vitamin The vitamin.
     * @return The part number.
     */
    fun partNumberFor(vitamin: Vitamin): String

    /**
     * Queries the price for some number of vitamins.
     *
     * @param vitamin The vitamin.
     * @param count The number of that vitamin.
     * @return The price.
     */
    fun priceFor(vitamin: Vitamin, count: Int): Double
}
