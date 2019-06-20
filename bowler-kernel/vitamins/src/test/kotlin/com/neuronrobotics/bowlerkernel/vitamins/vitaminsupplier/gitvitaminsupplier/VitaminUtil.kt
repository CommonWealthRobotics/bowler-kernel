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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBallBearing
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBattery
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultCapScrew
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultCompressionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultNut
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultTimingBelt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultTorsionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultVexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexAngle
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexCChannel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexMetal
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexPlate
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonStepperMotor
import org.junit.jupiter.api.Assertions.fail
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
    randomVexCChannel(),
    randomVexPlate(),
    randomVexAngle(),
    KlaxonDCMotor.fromVitamin(randomDCMotor()),
    KlaxonServo.fromVitamin(randomServo()),
    KlaxonStepperMotor.fromVitamin(randomStepperMotor()),
    KlaxonRoundMotor.fromVitamin(randomRoundMotor())
) + allVexWheels() + allShafts().map { KlaxonShaft.fromVitamin(it) }

internal fun <T : Random> T.randomCenterOfMass() =
    CenterOfMass(nextDouble().inch, nextDouble().inch, nextDouble().inch)

internal fun <T : Random> T.randomMap(): ImmutableMap<String, Any> =
    immutableMapOf(nextDouble().toString() to nextDouble())

internal fun <T : Random> T.randomVexMetal() =
    if (nextBoolean()) VexMetal.ALUMINUM else VexMetal.STEEL

internal fun <T : Random> T.randomBallBearing() =
    DefaultBallBearing(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomBattery() =
    DefaultBattery(
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

internal fun <T : Random> T.randomBolt() =
    DefaultBolt(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomCapScrew() =
    DefaultCapScrew(
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

internal fun <T : Random> T.randomCompressionSpring() =
    DefaultCompressionSpring(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        Stiffness(nextDouble()),
        nextDouble().gram,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomNut() =
    DefaultNut(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomTorsionSpring() =
    DefaultTorsionSpring(
        nextDouble().inch,
        nextDouble().degree,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        Stiffness(nextDouble()),
        nextDouble().gram,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomTimingBelt() =
    DefaultTimingBelt(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )

internal fun <T : Random> T.randomRoundMotor() =
    DefaultRoundMotor(
        nextDouble().inch,
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

internal fun <T : Random> T.randomDCMotor() =
    DefaultDCMotor(
        nextDouble().inch,
        nextDouble().inch,
        randomShaft(),
        nextDouble().inch,
        nextDouble().inch,
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

internal fun <T : Random> T.randomServo() =
    DefaultServo(
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

internal fun <T : Random> T.allShafts() = listOf(
    DefaultShaft.SquareShaft(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    ),
    DefaultShaft.RoundShaft(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    ),
    DefaultShaft.DShaft(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    ),
    DefaultShaft.ServoHorn.Arm(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextInt(),
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    ),
    DefaultShaft.ServoHorn.Wheel(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
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

internal fun <T : Random> T.randomStepperMotor() =
    DefaultStepperMotor(
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

internal fun <T : Random> T.randomVexCChannel() = VexCChannel(
    randomVexMetal(),
    nextInt(),
    nextInt(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomVexPlate() = VexPlate(
    randomVexMetal(),
    nextInt(),
    nextInt(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun <T : Random> T.randomVexAngle() = VexAngle(
    randomVexMetal(),
    nextInt(),
    nextInt(),
    nextDouble().gram,
    randomCenterOfMass(),
    randomMap()
)

internal fun allVexWheels(): List<DefaultVexWheel> = listOf(
    DefaultVexWheel.OmniWheel.Omni275,
    DefaultVexWheel.OmniWheel.Omni325,
    DefaultVexWheel.OmniWheel.Omni4,
    DefaultVexWheel.TractionWheel.Wheel275,
    DefaultVexWheel.TractionWheel.Wheel325,
    DefaultVexWheel.TractionWheel.Wheel4,
    DefaultVexWheel.TractionWheel.Wheel5,
    DefaultVexWheel.HighTraction,
    DefaultVexWheel.Mecanum,
    DefaultVexWheel.WheelLeg
)

@SuppressWarnings("ComplexMethod")
internal fun <T : Random> T.randomVexWheel(): DefaultVexWheel =
    when (nextInt(5)) {
        0 -> when (nextInt(3)) {
            0 -> DefaultVexWheel.OmniWheel.Omni275
            1 -> DefaultVexWheel.OmniWheel.Omni325
            2 -> DefaultVexWheel.OmniWheel.Omni4
            else -> fail { "" }
        }

        1 -> when (nextInt(4)) {
            0 -> DefaultVexWheel.TractionWheel.Wheel275
            1 -> DefaultVexWheel.TractionWheel.Wheel325
            2 -> DefaultVexWheel.TractionWheel.Wheel4
            3 -> DefaultVexWheel.TractionWheel.Wheel5
            else -> fail { "" }
        }

        2 -> DefaultVexWheel.HighTraction
        3 -> DefaultVexWheel.Mecanum
        4 -> DefaultVexWheel.WheelLeg
        else -> fail { "" }
    }
