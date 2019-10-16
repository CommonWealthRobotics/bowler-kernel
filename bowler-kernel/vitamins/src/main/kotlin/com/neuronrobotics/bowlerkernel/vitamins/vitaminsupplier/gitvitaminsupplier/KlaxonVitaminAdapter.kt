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

import com.beust.klaxon.TypeAdapter
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexAngle
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexCChannel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexEDRMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexPlate
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBallBearing
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBattery
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultCapScrew
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultCompressionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultNut
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultTimingBelt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultTorsionSpring
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultVexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonRoundMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import kotlin.reflect.KClass
import org.octogonapus.ktguava.collections.toImmutableMap

/**
 * Adapts [KlaxonVitaminTo] for [KlaxonGitVitamin.type] so Klaxon can handle polymorphism.
 */
class KlaxonVitaminAdapter : TypeAdapter<KlaxonVitaminTo> {

    @SuppressWarnings("ComplexMethod")
    override fun classFor(type: Any): KClass<out KlaxonVitaminTo> =
        classesMap[type as String] ?: throw IllegalArgumentException("Unknown type: $type")

    companion object {
        private val classesSet = setOf(
            DefaultBallBearing::class,
            DefaultBattery::class,
            DefaultBolt::class,
            DefaultCapScrew::class,
            DefaultCompressionSpring::class,
            DefaultTorsionSpring::class,
            DefaultNut::class,
            DefaultTimingBelt::class,
            DefaultVexWheel::class,
            DefaultVexWheel.OmniWheel::class,
            DefaultVexWheel.OmniWheel.Omni275::class,
            DefaultVexWheel.OmniWheel.Omni325::class,
            DefaultVexWheel.OmniWheel.Omni4::class,
            DefaultVexWheel.TractionWheel::class,
            DefaultVexWheel.TractionWheel.Wheel275::class,
            DefaultVexWheel.TractionWheel.Wheel325::class,
            DefaultVexWheel.TractionWheel.Wheel4::class,
            DefaultVexWheel.TractionWheel.Wheel5::class,
            DefaultVexWheel.HighTraction::class,
            DefaultVexWheel.Mecanum::class,
            DefaultVexWheel.WheelLeg::class,
            VexAngle::class,
            VexCChannel::class,
            VexEDRMotor::class,
            VexEDRMotor.VexMotor393::class,
            VexEDRMotor.VexMotor269::class,
            VexPlate::class,
            KlaxonDCMotor::class,
            KlaxonServo::class,
            KlaxonShaft::class,
            KlaxonStepperMotor::class,
            KlaxonRoundMotor::class
        )

        private val classesMap = classesSet.map { it.qualifiedName!! to it }.toImmutableMap()
    }
}
