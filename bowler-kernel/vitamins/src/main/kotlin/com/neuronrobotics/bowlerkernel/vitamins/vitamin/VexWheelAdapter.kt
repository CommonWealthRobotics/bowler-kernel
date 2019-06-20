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

import com.beust.klaxon.TypeAdapter
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.allNestedSubclasses
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.plus
import kotlin.reflect.KClass

/**
 * A Klaxon [TypeAdapter] for [DefaultShaft]. Reflectively reads all [DefaultVexWheel] nested
 * subclasses to generate the [KlaxonVexWheel.wheelType] number.
 */
class VexWheelAdapter : TypeAdapter<DefaultVexWheel> {

    private val wheelTypes: ImmutableList<KClass<out DefaultVexWheel>> by lazy {
        immutableListOf(DefaultVexWheel::class) + allNestedSubclasses(DefaultVexWheel::class)
    }

    override fun classFor(type: Any): KClass<out DefaultVexWheel> {
        val index = type as Int

        require(index >= 0)
        require(index < wheelTypes.size)

        return wheelTypes[index]
    }

    fun typeFor(wheelClass: KClass<out VexWheel>): Int =
        wheelTypes.indexOf(wheelClass).also {
            require(it >= 0)
        }
}
