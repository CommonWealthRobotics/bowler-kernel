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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class GitVitaminSupplierTest {

    private val vitamins = with(Random) {
        immutableSetOf(
            randomBallBearing(),
            randomBattery(),
            randomCapScrew(),
            randomDCMotor(),
            randomServo(),
            randomShaft(),
            randomStepperMotor()
        )
    }

    private val partNumbers = vitamins.map { it to it::class.toString() }.toImmutableMap()

    private val prices = vitamins.map { it to Random.nextDouble() }.toImmutableMap()

    private val supplier = GitVitaminSupplier("name", vitamins, partNumbers, prices)

    @Test
    fun `test part numbers`() {
        assertAll(vitamins.map {
            {
                assertEquals(
                    partNumbers[it],
                    supplier.partNumberFor(it)
                )
            }
        })
    }

    @Test
    fun `test unknown part number`() {
        assertThrows<IllegalArgumentException> {
            supplier.partNumberFor(Random.randomBallBearing())
        }
    }

    @Test
    fun `test prices`() {
        assertAll(vitamins.map {
            {
                assertEquals(
                    prices[it]!! * 2,
                    supplier.priceFor(it, 2)
                )
            }
        })
    }

    @Test
    fun `test unknown price`() {
        assertThrows<IllegalArgumentException> {
            supplier.priceFor(Random.randomBallBearing(), 1)
        }
    }
}
