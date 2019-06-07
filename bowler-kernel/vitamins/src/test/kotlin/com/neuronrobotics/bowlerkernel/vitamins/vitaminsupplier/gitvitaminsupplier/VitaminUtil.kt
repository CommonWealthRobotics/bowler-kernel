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

import com.google.common.collect.ImmutableMap
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBallBearing
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBattery
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultCapScrew
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultCompressionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultNut
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultTimingBelt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultTorsionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonStepperMotor
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktunits.quantities.Stiffness
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.coulomb
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.nM
import org.octogonapus.ktunits.quantities.radianPerMinute
import org.octogonapus.ktunits.quantities.volt
import org.octogonapus.ktunits.quantities.watt
import kotlin.random.Random

internal fun <T : Random> T.allVitamins() = listOf(
    randomBallBearing(),
    randomBattery(),
    randomBolt(),
    randomCapScrew(),
    randomCompressionSpring(),
    randomNut(),
    randomTorsionSpring(),
    randomTimingBelt(),
    KlaxonDCMotor.fromVitamin(randomDCMotor()),
    KlaxonServo.fromVitamin(randomServo()),
    KlaxonShaft.fromVitamin(randomShaft()),
    KlaxonStepperMotor.fromVitamin(randomStepperMotor()),
    KlaxonRoundMotor.fromVitamin(randomRoundMotor())
)

internal fun <T : Random> T.randomCenterOfMass() =
    CenterOfMass(nextDouble().inch, nextDouble().inch, nextDouble().inch)

internal fun <T : Random> T.randomMap(): ImmutableMap<String, Any> =
    immutableMapOf(nextDouble().toString() to nextDouble())

internal fun <T : Random> T.randomBallBearing() = DefaultBallBearing(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomBattery() = DefaultBattery(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().volt,
    nextDouble().ampere,
    nextDouble().coulomb,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomBolt() = DefaultBolt(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomCapScrew() = DefaultCapScrew(
    nextInt(),
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomCompressionSpring() = DefaultCompressionSpring(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    Stiffness(nextDouble()),
    nextDouble().gram,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomNut() = DefaultNut(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomTorsionSpring() = DefaultTorsionSpring(
    nextDouble().inch,
    nextDouble().degree,
    nextDouble().inch,
    nextDouble().inch,
    Stiffness(nextDouble()),
    nextDouble().gram,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomTimingBelt() = DefaultTimingBelt(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomRoundMotor() = DefaultRoundMotor(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    randomShaft(),
    randomBolt(),
    nextDouble().inch,
    nextDouble().degree,
    nextDouble().degree,
    nextDouble().volt,
    nextDouble().radianPerMinute,
    nextDouble().ampere,
    nextDouble().nM,
    nextDouble().ampere,
    nextDouble().watt,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomDCMotor() = DefaultDCMotor(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().volt,
    nextDouble().radianPerMinute,
    nextDouble().ampere,
    nextDouble().nM,
    nextDouble().ampere,
    nextDouble().watt,
    randomShaft(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomServo() = DefaultServo(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().volt,
    nextDouble().nM,
    nextDouble().radianPerMinute,
    randomShaft(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomShaft() = DefaultShaft.ServoHorn.Arm(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    nextInt(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomStepperMotor() = DefaultStepperMotor(
    nextDouble().inch,
    nextDouble().inch,
    nextDouble().inch,
    randomBolt(),
    randomShaft(),
    nextDouble().volt,
    nextDouble().nM,
    nextDouble().ampere,
    nextDouble().degree,
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)
