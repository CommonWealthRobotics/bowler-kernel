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

import arrow.core.Try
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.GitFS
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultServo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.div
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.kgFCm
import org.octogonapus.ktunits.quantities.second
import org.octogonapus.ktunits.quantities.volt
import java.io.File

internal class GitVitaminSupplierFactoryTest {

    private val firstVitamin = DefaultServo(
        6.volt,
        2.kgFCm,
        60.degree / 0.12.second,
        1.3.inch,
        0.5.inch,
        0.9.inch,
        12.gram,
        CenterOfMass(
            1.3.inch / 2,
            0.5.inch / 2,
            0.9.inch / 2
        ),
        immutableMapOf("feedback" to "supported"),
        GitFile("", "")
    )

    private val supplierName = "test-supplier"
    private val firstFileName = "vitamin1.json"
    private val supplierFile = GitFile(
        "vitamins.git",
        "vitamins.json"
    )

    private fun makeMockGitFS(files: ImmutableList<File>) =
        mock<GitFS> { on { cloneRepoAndGetFiles(supplierFile.gitUrl) } doReturn Try { files } }

    @Test
    fun `test loading vitamins`(@TempDir tempDir: File) {
        val mockGitFS = makeMockGitFS(
            immutableListOf(
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
                                  },
                                  "height": {
                                    "value": 0.02286,
                                    "angleDim": 0,
                                    "currentDim": 0,
                                    "lengthDim": 1,
                                    "luminDim": 0,
                                    "massDim": 0,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": 0
                                  },
                                  "length": {
                                    "value": 0.0127,
                                    "angleDim": 0,
                                    "currentDim": 0,
                                    "lengthDim": 1,
                                    "luminDim": 0,
                                    "massDim": 0,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": 0
                                  },
                                  "mass": {
                                    "value": 0.012,
                                    "angleDim": 0,
                                    "currentDim": 0,
                                    "lengthDim": 0,
                                    "luminDim": 0,
                                    "massDim": 1,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": 0
                                  },
                                  "specs": {
                                    "feedback": "supported"
                                  },
                                  "speed": {
                                    "value": 8.726646259971647,
                                    "angleDim": 1,
                                    "currentDim": 0,
                                    "lengthDim": 0,
                                    "luminDim": 0,
                                    "massDim": 0,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": -1
                                  },
                                  "stallTorque": {
                                    "value": 0.19614,
                                    "angleDim": 0,
                                    "currentDim": 0,
                                    "lengthDim": 2,
                                    "luminDim": 0,
                                    "massDim": 1,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": -2
                                  },
                                  "voltage": {
                                    "value": 6,
                                    "angleDim": 0,
                                    "currentDim": -1,
                                    "lengthDim": 2,
                                    "luminDim": 0,
                                    "massDim": 1,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": -3
                                  },
                                  "width": {
                                    "value": 0.03302,
                                    "angleDim": 0,
                                    "currentDim": 0,
                                    "lengthDim": 1,
                                    "luminDim": 0,
                                    "massDim": 0,
                                    "moleDim": 0,
                                    "tempDim": 0,
                                    "timeDim": 0
                                  }
                                },
                              "partNumber": "abcd",
                              "price": 3.5
                            }
                        """.trimIndent()
                    )
                }
            )
        )

        val result = GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)

        assertAll(
            { assertEquals(supplierName, result.name) },
            { assertEquals(setOf(firstVitamin), result.allVitamins) },
            { assertEquals(mapOf(firstVitamin to "abcd"), result.partNumbers) },
            { assertEquals(mapOf(firstVitamin to 3.5), result.prices) }
        )
    }

    @Test
    fun `test unable to clone repo`() {
        val mockGitFS = mock<GitFS> {
            on { cloneRepoAndGetFiles(any(), any()) } doReturn Try {
                throw IllegalStateException("Oops!")
            }
        }

        assertThrows<IllegalStateException> {
            GitVitaminSupplierFactory(mockGitFS).createVitaminSupplier(supplierFile)
        }
    }

    @Test
    fun `test unable to parse GitVitaminSupplierData`(@TempDir tempDir: File) {
        val mockGitFS = makeMockGitFS(
            immutableListOf(
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
            immutableListOf(
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
