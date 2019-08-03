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
package com.neuronrobotics.bowlercad.vitamin

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import eu.mihosoft.vrl.v3d.CSG

/**
 * A CAD generator which operates on Vitamins.
 *
 * @param T The Vitamin type.
 */
interface VitaminCadGenerator<T : Vitamin> {

    /**
     * Generates the CAD for this [Vitamin]. This return value may be cached by this generator.
     *
     * @param vitamin The [Vitamin].
     * @return The CAD for this [Vitamin].
     */
    fun generateCAD(vitamin: T): CSG
}
