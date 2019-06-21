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
import com.beust.klaxon.TypeFor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.getConfiguredKlaxonWithoutGitVitaminConverter
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.sealedObjectHierarchies

/**
 * A vitamin that Klaxon can parse.
 *
 * @param type The type of this vitamin, used by Klaxon to handle polymorphism.
 * @param vitamin The vitamin.
 * @param partNumber The part number.
 * @param price The price for ne unit.
 */
data class KlaxonGitVitamin
private constructor(
    @TypeFor(field = "vitamin", adapter = KlaxonVitaminAdapter::class)
    val type: String,
    val vitamin: KlaxonVitaminTo,
    val partNumber: String,
    val price: Double
) {

    companion object : Converter {

        fun from(other: KlaxonVitaminTo, partNumber: String, price: Double) =
            KlaxonGitVitamin(
                type = other::class.qualifiedName!!,
                vitamin = other,
                partNumber = partNumber,
                price = price
            )

        override fun canConvert(cls: Class<*>) = cls == KlaxonGitVitamin::class.java

        override fun fromJson(jv: JsonValue): Any? {
            with(jv.obj!!) {
                val vitaminObj = obj("vitamin")!!
                val vitaminObjName = vitaminObj["name"] as String?

                // We can't have the KlaxonGitVitamin converter applied to this instance because it would
                // cause infinite converter recursion.
                val klaxon = getConfiguredKlaxonWithoutGitVitaminConverter()

                // Check if any of the known hierarchies is correct and pick the first correct one.
                // If none are correct, parse using the default converter.
                val vitamin = sealedObjectHierarchies.mapNotNull {
                    if (vitaminObjName?.contains(it.qualifiedName!!) == true) {
                        klaxon.fromJsonObject(vitaminObj, it.java, it) as KlaxonVitaminTo
                    } else {
                        null
                    }
                }.firstOrNull() ?: return klaxon.parseFromJsonObject<KlaxonGitVitamin>(this)!!

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

            // We can't have the KlaxonGitVitamin converter applied to this instance because it would
            // cause infinite converter recursion.
            val klaxon = getConfiguredKlaxonWithoutGitVitaminConverter()

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
