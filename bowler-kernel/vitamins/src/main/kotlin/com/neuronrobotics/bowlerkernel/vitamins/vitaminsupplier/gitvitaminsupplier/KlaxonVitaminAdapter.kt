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
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBallBearing
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBattery
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultBolt
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultCapScrew
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.DefaultNut
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.Vitamin
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonDCMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonServo
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonShaft
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonStepperMotor
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import kotlin.reflect.KClass

/**
 * Adapts [Vitamin] and [KlaxonGitVitamin] so Klaxon can handle polymorphism.
 */
class KlaxonVitaminAdapter : TypeAdapter<KlaxonVitaminTo> {

    @SuppressWarnings("ComplexMethod")
    override fun classFor(type: Any): KClass<out KlaxonVitaminTo> {
        return when (type as String) {
            "DefaultBallBearing" -> DefaultBallBearing::class
            "DefaultBattery" -> DefaultBattery::class
            "DefaultBolt" -> DefaultBolt::class
            "DefaultCapScrew" -> DefaultCapScrew::class
            "DefaultNut" -> DefaultNut::class
            "KlaxonDCMotor" -> KlaxonDCMotor::class
            "KlaxonServo" -> KlaxonServo::class
            "KlaxonShaft" -> KlaxonShaft::class
            "KlaxonStepperMotor" -> KlaxonStepperMotor::class
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
    }
}
