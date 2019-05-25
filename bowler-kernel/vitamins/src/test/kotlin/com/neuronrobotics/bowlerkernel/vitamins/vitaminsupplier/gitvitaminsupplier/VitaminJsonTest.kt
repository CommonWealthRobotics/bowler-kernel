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

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBallBearing
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBattery
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonStepperMotor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktguava.klaxon.immutableMapConverter
import kotlin.random.Random

internal class VitaminJsonTest {

    private val klaxon = Klaxon().apply {
        fieldConverter(ConvertImmutableMap::class, immutableMapConverter())
    }

    @Test
    fun `test converting ball bearing`() {
        Random.apply {
            val vitaminBefore = randomBallBearing()
            val jsonString = klaxon.toJsonString(vitaminBefore)
            val vitaminAfter = klaxon.parse<DefaultBallBearing>(jsonString)
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @Test
    fun `test converting battery`() {
        Random.apply {
            val vitaminBefore = randomBattery()
            val jsonString = klaxon.toJsonString(vitaminBefore)
            val vitaminAfter = klaxon.parse<DefaultBattery>(jsonString)
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @Test
    fun `test converting dc motor`() {
        Random.apply {
            val vitaminBefore = randomDCMotor()
            val jsonString = klaxon.toJsonString(KlaxonDCMotor.fromVitamin(vitaminBefore))
            val vitaminAfter = klaxon.parse<KlaxonDCMotor>(jsonString)!!.toVitamin()
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @Test
    fun `test converting servo`() {
        Random.apply {
            val vitaminBefore = randomServo()
            val jsonString = klaxon.toJsonString(KlaxonServo.fromVitamin(vitaminBefore))
            val vitaminAfter = klaxon.parse<KlaxonServo>(jsonString)!!.toVitamin()
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @Test
    fun `test converting shaft`() {
        Random.apply {
            val vitaminBefore = randomShaft()
            val jsonString = klaxon.toJsonString(vitaminBefore)
            val vitaminAfter = klaxon.parse<DefaultShaft.ServoHorn.DoubleArm>(jsonString)
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }

    @Test
    fun `test converting stepper motor`() {
        Random.apply {
            val vitaminBefore = randomStepperMotor()
            val jsonString = klaxon.toJsonString(KlaxonStepperMotor.fromVitamin(vitaminBefore))
            val vitaminAfter = klaxon.parse<KlaxonStepperMotor>(jsonString)!!.toVitamin()
            assertEquals(vitaminBefore, vitaminAfter)
        }
    }
}
