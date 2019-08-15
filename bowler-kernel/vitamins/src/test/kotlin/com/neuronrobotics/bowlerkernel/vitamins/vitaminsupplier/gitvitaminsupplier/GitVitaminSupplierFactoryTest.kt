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

import arrow.effects.IO
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.getConfiguredKlaxon
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.plus
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class GitVitaminSupplierFactoryTest {

    private val klaxon = getConfiguredKlaxon()

    private val vitamins = with(Random) { allVitamins() }

    private val klaxonVitamins = vitamins.map {
        KlaxonGitVitamin.from(
            other = it,
            partNumber = it::class.toString(),
            price = Random.nextDouble()
        )
    }

    private val supplierName = "test-supplier"
    private val firstFileName = "vitamin1.json"
    private val supplierFile = GitFile(
        "vitamins.git",
        "vitamins.json"
    )

    private fun makeMockGitFS(files: ImmutableSet<File>) =
        mock<GitFS> { on { cloneRepoAndGetFiles(supplierFile.gitUrl) } doReturn IO.just(files) }

    @Test
    fun `test loading vitamins`(@TempDir tempDir: File) {
        // Generate unique file names so that vitamins with the same class don't overwrite each
        // other
        val fileNames = klaxonVitamins.map {
            it to Random.nextBytes(32).joinToString(separator = "")
        }.toMap()

        val mockGitFS = makeMockGitFS(
            immutableSetOf(
                File(tempDir, supplierFile.filename).apply {
                    writeText(
                        """
                            {
                              "name": "$supplierName",
                              "files": [
                                ${klaxonVitamins.joinToString(",") {
                            "\"${fileNames[it]}.json\""
                        }}
                              ]
                            }
                        """.trimIndent()
                    )
                }
            ) + klaxonVitamins.map {
                File(tempDir, "${fileNames[it]}.json").apply {
                    writeText(klaxon.toJsonString(it))
                }
            }
        )

        val result = GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)

        assertAll(
            { assertEquals(supplierName, result.name) },
            { assertEquals(vitamins.map { it.toVitamin() }.toSet(), result.allVitamins) },
            {
                assertEquals(
                    klaxonVitamins.map { it.vitamin.toVitamin() to it.partNumber }.toMap(),
                    result.partNumbers
                )
            },
            {
                assertEquals(
                    klaxonVitamins.map { it.vitamin.toVitamin() to it.price }.toMap(),
                    result.prices
                )
            }
        )
    }

    @Test
    fun `test unable to clone repo`() {
        val mockGitFS = mock<GitFS> {
            on {
                cloneRepoAndGetFiles(any(), any())
            } doReturn IO.raiseError(IllegalStateException("Oops!"))
        }

        assertThrows<IllegalStateException> {
            GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)
        }
    }

    @Test
    fun `test unable to parse GitVitaminSupplierData`(@TempDir tempDir: File) {
        val mockGitFS = makeMockGitFS(
            immutableSetOf(
                // Invalid supplier file
                File(tempDir, supplierFile.filename).apply {
                    writeText(
                        """
                            {
                              "name": "$supplierName"
                            }
                        """.trimIndent()
                    )
                }
            )
        )

        assertThrows<IllegalStateException> {
            GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)
        }
    }

    @Test
    fun `test unable to parse a vitamin`(@TempDir tempDir: File) {
        val mockGitFS = makeMockGitFS(
            immutableSetOf(
                File(tempDir, supplierFile.filename).apply {
                    writeText(
                        """
                            {
                              "name": "$supplierName",
                              "files": [
                                "$firstFileName"
                              ]
                            }
                        """.trimIndent()
                    )
                },
                // Invalid vitamin file
                File(tempDir, firstFileName).apply {
                    writeText(
                        """
                            {
                              "type": "defaultServo",
                              "vitamin": {
                                "cadGenerator": {
                                  "filename": "",
                                  "gitUrl": ""
                                },
                                "centerOfMass": {
                                    "x": {
                                      "value": 0.01651,
                                      "angleDim": 0,
                                      "currentDim": 0,
                                      "lengthDim": 1,
                                      "luminDim": 0,
                                      "massDim": 0,
                                      "moleDim": 0,
                                      "tempDim": 0,
                                      "timeDim": 0
                                    },
                                    "y": {
                                      "value": 0.00635,
                                      "angleDim": 0,
                                      "currentDim": 0,
                                      "lengthDim": 1,
                                      "luminDim": 0,
                                      "massDim": 0,
                                      "moleDim": 0,
                                      "tempDim": 0,
                                      "timeDim": 0
                                    },
                                    "z": {
                                      "value": 0.01143,
                                      "angleDim": 0,
                                      "currentDim": 0,
                                      "lengthDim": 1,
                                      "luminDim": 0,
                                      "massDim": 0,
                                      "moleDim": 0,
                                      "tempDim": 0,
                                      "timeDim": 0
                                    }
                                  }
                            }
                            """.trimIndent()
                    )
                }
            )
        )

        assertThrows<IllegalStateException> {
            GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)
        }
    }
}
