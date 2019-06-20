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

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.TypeFor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultVexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.NestedObjectConverter
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktguava.klaxon.immutableMapConverter

data class KlaxonGitVitamin
private constructor(
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

    companion object : Converter {

        // We can't have the KlaxonGitVitamin converter applied to this instance
        private val klaxon = Klaxon().apply {
            fieldConverter(ConvertImmutableMap::class, immutableMapConverter())
            converter(NestedObjectConverter(DefaultVexWheel::class))
        }

        fun from(other: KlaxonVitaminTo, partNumber: String, price: Double) =
            KlaxonGitVitamin(
                type = other::class.simpleName!!,
                vitamin = other,
                partNumber = partNumber,
                price = price
            )

        override fun canConvert(cls: Class<*>) = cls == KlaxonGitVitamin::class.java

        override fun fromJson(jv: JsonValue): Any? {
            with(jv.obj!!) {
                val vitaminObj = obj("vitamin")!!
                val vitaminObjName = vitaminObj["name"] as String?
                val vitamin =
                    if (vitaminObjName?.contains(DefaultVexWheel::class.simpleName!!) == true) {
                        klaxon.parseFromJsonObject<DefaultVexWheel>(vitaminObj)!!
                    } else {
                        // We need to use the type adapter so do a full re-parse. This is fine
                        // because now we know that the vitamin is not a DefaultVexWheel
                        return klaxon.parseFromJsonObject<KlaxonGitVitamin>(this)!!
                    }

                return KlaxonGitVitamin(
                    string("type")!!,
                    vitamin,
                    string("partNumber")!!,
                    double("price")!!
                )
            }
        }

        override fun toJson(value: Any): String {
            require(value is KlaxonGitVitamin)
            return """
                {
                    "type": "${value.type}",
                    "vitamin": ${klaxon.toJsonString(value.vitamin)},
                    "partNumber": "${value.partNumber}",
                    "price": ${value.price}
                }
            """.trimIndent()
        }
    }
}
