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

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultVexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.NestedObjectConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktguava.klaxon.immutableMapConverter
import kotlin.random.Random
import kotlin.reflect.KClass

internal class VitaminJsonTest {

    private val klaxon = Klaxon().apply {
        fieldConverter(ConvertImmutableMap::class, immutableMapConverter())
        converter(NestedObjectConverter(DefaultVexWheel::class))
    }

    private fun Klaxon.parse(json: String, clazz: KClass<*>): KlaxonVitaminTo {
        val jsonObject = parser(clazz).parse(json.byteInputStream()) as JsonObject
        val parsedObject = fromJsonObject(jsonObject, clazz.java, clazz)
        return parsedObject as? KlaxonVitaminTo ?: fail {
            "Could not cast $parsedObject to $clazz"
        }
    }

    private fun testConversion(vitaminBefore: Vitamin, clazz: KClass<*>) {
        Random.apply {
            val jsonString = klaxon.toJsonString(vitaminBefore)
            val vitaminAfter = klaxon.parse(jsonString, clazz)
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @ParameterizedTest
    @MethodSource("vitaminSource")
    fun `test converting vitamins`(vitaminBefore: KlaxonVitaminTo) {
        testConversion(vitaminBefore, vitaminBefore::class)
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun vitaminSource() = with(Random) { allVitamins() }
    }
}
