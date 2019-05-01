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

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin

/**
 * A [Vitamin] in Git that Klaxon can parse into.
 */
interface KlaxonGitVitamin {

    /**
     * The type of this vitamin, used by Klaxon to handle polymorphism. MUST be annotated with
     * Klaxon's TypeFor.
     */
    val type: String

    /**
     * The vitamin.
     */
    val vitamin: Vitamin

    /**
     * The part number.
     */
    val partNumber: String

    /**
     * The price for one unit.
     */
    val price: Double
}
