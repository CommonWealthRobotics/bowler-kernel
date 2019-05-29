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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultStepperMotor
import org.octogonapus.ktguava.collections.immutableMapOf
import org.octogonapus.ktunits.quantities.ampere
import org.octogonapus.ktunits.quantities.coulomb
import org.octogonapus.ktunits.quantities.degree
import org.octogonapus.ktunits.quantities.gram
import org.octogonapus.ktunits.quantities.hz
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.nM
import org.octogonapus.ktunits.quantities.radianPerMinute
import org.octogonapus.ktunits.quantities.volt
import org.octogonapus.ktunits.quantities.watt
import kotlin.random.Random

internal fun Random.randomCenterOfMass() =
    CenterOfMass(nextDouble().inch, nextDouble().inch, nextDouble().inch)

internal fun Random.randomMap(): ImmutableMap<String, Any> =
    immutableMapOf(nextDouble().toString() to nextDouble())

internal fun Random.Default.randomBallBearing(): DefaultBallBearing {
    return DefaultBallBearing(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}

internal fun Random.Default.randomBattery(): DefaultBattery {
    return DefaultBattery(
        nextDouble().volt,
        nextDouble().ampere,
        nextDouble().hz,
        nextDouble().coulomb,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}

internal fun Random.Default.randomDCMotor(): DefaultDCMotor {
    return DefaultDCMotor(
        nextDouble().volt,
        nextDouble().radianPerMinute,
        nextDouble().ampere,
        nextDouble().nM,
        nextDouble().ampere,
        nextDouble().watt,
        DefaultShaft.DShaft(
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().gram,
            randomCenterOfMass(),
            randomMap()
        ),
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}

internal fun Random.Default.randomServo(): DefaultServo {
    return DefaultServo(
        nextDouble().volt,
        nextDouble().nM,
        nextDouble().radianPerMinute,
        DefaultShaft.ServoHorn.XArm(
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().gram,
            randomCenterOfMass(),
            randomMap()
        ),
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}

internal fun Random.Default.randomShaft(): DefaultShaft.ServoHorn.DoubleArm {
    return DefaultShaft.ServoHorn.DoubleArm(
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}

internal fun Random.Default.randomStepperMotor(): DefaultStepperMotor {
    return DefaultStepperMotor(
        1,
        nextDouble().volt,
        nextDouble().nM,
        nextDouble().ampere,
        nextDouble().degree,
        DefaultShaft.DShaft(
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().inch,
            nextDouble().gram,
            randomCenterOfMass(),
            randomMap()
        ),
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().inch,
        nextDouble().gram,
        randomCenterOfMass(),
        randomMap()
    )
}
