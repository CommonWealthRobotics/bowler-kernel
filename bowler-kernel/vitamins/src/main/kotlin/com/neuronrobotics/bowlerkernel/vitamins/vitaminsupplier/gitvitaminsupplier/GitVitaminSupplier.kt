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

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier.VitaminSupplier

class GitVitaminSupplier(
    override val name: String,
    override val allVitamins: ImmutableSet<Vitamin>,
    val partNumbers: ImmutableMap<Vitamin, String>,
    val prices: ImmutableMap<Vitamin, Double>
) : VitaminSupplier {

    override fun partNumberFor(vitamin: Vitamin) =
        partNumbers[vitamin] ?: throw IllegalArgumentException(
            """
            |Unknown vitamin:
            |$vitamin
            """.trimMargin()
        )

    override fun priceFor(vitamin: Vitamin, count: Int) =
        prices[vitamin]?.let { it * count } ?: throw IllegalArgumentException(
            """
            |Unknown vitamin:
            |$vitamin
            """.trimMargin()
        )
}
