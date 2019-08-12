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

import arrow.core.getOrHandle
import arrow.effects.extensions.io.monadDefer.bindingCatch
import com.beust.klaxon.JsonObject
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.getConfiguredKlaxon
import com.neuronrobotics.bowlerkernel.vitamins.vitaminsupplier.VitaminSupplierFactory
import org.octogonapus.ktguava.collections.toImmutableMap
import org.octogonapus.ktguava.collections.toImmutableSet
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass

/**
 * Creates [GitVitaminSupplier] using a supplier file in a Git repository. The expected
 * repository format is:
 *  - The file passed to [createVitaminSupplier] can be parsed into a [GitVitaminSupplierData].
 *  - Each file in [GitVitaminSupplierData.files] can be parsed into a [KlaxonGitVitamin].
 */
@SuppressWarnings("LargeClass")
class GitVitaminSupplierFactory(
    private val gitFS: GitFS,
    private val vitaminType: KClass<out KlaxonGitVitamin> = KlaxonGitVitamin::class
) : VitaminSupplierFactory<GitVitaminSupplier> {

    private val klaxon = getConfiguredKlaxon()

    override fun createVitaminSupplier(vitaminSupplierFile: GitFile): GitVitaminSupplier {
        val allVitaminsFromGit = bindingCatch {
            val (allFiles) = gitFS.cloneRepoAndGetFiles(vitaminSupplierFile.gitUrl)

            val vitaminsFile = allFiles.first { it.name == vitaminSupplierFile.filename }

            val gitVitamins = FileReader(vitaminsFile).use { reader ->
                klaxon.parse<GitVitaminSupplierData>(reader) ?: throw IllegalStateException(
                    """
                    |Could not parse GitVitaminSupplierData from file:
                    |$vitaminsFile
                    """.trimMargin()
                )
            }

            // Search independent of the repo's actual position on disk by only looking for a
            // matching suffix
            val allVitaminFiles = gitVitamins.files.map { vitaminFilePath ->
                allFiles.first { it.path.endsWith(vitaminFilePath) }
            }

            gitVitamins.name to allVitaminFiles.map { parseVitamin(it) }
        }

        val (nameFromGit, vitaminsFromGit) = allVitaminsFromGit.attempt()
            .unsafeRunSync()
            .getOrHandle {
                throw IllegalStateException(
                    """
                    |Unable to load vitamins from git file:
                    |$vitaminSupplierFile
                    """.trimMargin(),
                    it
                )
            }

        val convertedVitaminsFromGit =
            vitaminsFromGit.map { it.vitamin.toVitamin() }.toImmutableSet()
        val bothVitamins = convertedVitaminsFromGit.zip(vitaminsFromGit)

        return GitVitaminSupplier(
            nameFromGit,
            convertedVitaminsFromGit,
            bothVitamins.map { it.first to it.second.partNumber }.toImmutableMap(),
            bothVitamins.map { it.first to it.second.price }.toImmutableMap()
        )
    }

    private fun parseVitamin(vitaminFile: File): KlaxonGitVitamin =
        FileReader(vitaminFile).use { reader ->
            // We have to parse this "by hand" because variables can't be used as reified
            // type parameters
            val jsonObject = klaxon.parser(vitaminType).parse(reader) as JsonObject
            val parsedObject =
                klaxon.fromJsonObject(jsonObject, vitaminType.java, vitaminType)

            parsedObject as KlaxonGitVitamin? ?: throw IllegalStateException(
                """
                |Could not parse DefaultKlaxonGitVitamin from file:
                |$vitaminFile
                """.trimMargin()
            )
        }
}
