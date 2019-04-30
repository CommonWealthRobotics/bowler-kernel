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

import arrow.core.extensions.`try`.monadThrow.bindingCatch
import arrow.core.getOrElse
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier.VitaminSupplierFactory
import org.octogonapus.ktguava.collections.toImmutableMap
import org.octogonapus.ktguava.collections.toImmutableSet
import java.io.FileReader
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
annotation class ConvertImmutableMap

/**
 * Creates [GitVitaminSupplier] using a supplier file in a Git repository. The expected
 * repository format is:
 *  - The file passed to [createVitaminSupplier] can be parsed into a [GitVitaminSupplierData].
 *  - Each file in [GitVitaminSupplierData.files] can be parsed into a [vitaminType].
 */
class GitVitaminSupplierFactory(
    private val gitFS: GitFS,
    private val vitaminType: KClass<out KlaxonGitVitamin> = DefaultKlaxonGitVitamin::class
) : VitaminSupplierFactory<GitVitaminSupplier> {

    private val klaxon = Klaxon()

    private val immutableMapConverter = object : Converter {
        override fun canConvert(cls: Class<*>) = cls == ImmutableMap::class.java

        override fun fromJson(jv: JsonValue) =
            klaxon.parseFromJsonObject<Map<*, *>>(jv.obj!!)!!.toImmutableMap()

        override fun toJson(value: Any) =
            klaxon.toJsonString(value as Map<*, *>)
    }

    init {
        klaxon.fieldConverter(ConvertImmutableMap::class, immutableMapConverter)
    }

    override fun createVitaminSupplier(vitaminSupplierFile: GitFile): GitVitaminSupplier {
        val allVitaminsFromGit = bindingCatch {
            val (allFiles) = gitFS.cloneRepoAndGetFiles(vitaminSupplierFile.gitUrl)

            val vitaminsFile = allFiles.first { it.name == vitaminSupplierFile.filename }

            val gitVitamins =
                klaxon.parse<GitVitaminSupplierData>(vitaminsFile) ?: throw IllegalStateException(
                    """
                    |Could not parse GitVitaminSupplierData from file:
                    |$vitaminsFile
                    """.trimMargin()
                )

            // Search independent of the repo's actual position on disk by only looking for a
            // matching suffix
            val allVitaminFiles = gitVitamins.files.map { vitaminFilePath ->
                allFiles.first { it.path.endsWith(vitaminFilePath) }
            }

            gitVitamins.name to allVitaminFiles.map {
                // We have to parse this "by hand" because variables can't be used as reified
                // type parameters
                val jsonObject = klaxon.parser(vitaminType).parse(FileReader(it)) as JsonObject
                val parsedObject = klaxon.fromJsonObject(jsonObject, vitaminType.java, vitaminType)
                parsedObject as KlaxonGitVitamin? ?: throw IllegalStateException(
                    """
                    |Could not parse DefaultKlaxonGitVitamin from file:
                    |$it
                    """.trimMargin()
                )
            }
        }

        val (nameFromGit, vitaminsFromGit) = allVitaminsFromGit.getOrElse {
            throw IllegalStateException(
                """
                |Unable to load vitamins from git file:
                |$vitaminSupplierFile
                """.trimMargin(),
                it
            )
        }

        return GitVitaminSupplier(
            nameFromGit,
            vitaminsFromGit.map { it.vitamin }.toImmutableSet(),
            vitaminsFromGit.map { it.vitamin to it.partNumber }.toImmutableMap(),
            vitaminsFromGit.map { it.vitamin to it.price }.toImmutableMap()
        )
    }
}
