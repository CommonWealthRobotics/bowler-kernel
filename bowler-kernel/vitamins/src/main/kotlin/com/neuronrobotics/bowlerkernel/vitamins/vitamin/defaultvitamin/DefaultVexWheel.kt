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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.VexWheel
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon.KlaxonVitaminTo
import org.octogonapus.ktguava.collections.emptyImmutableMap
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.inch
import org.octogonapus.ktunits.quantities.lbM

/**
 * VEX EDR brand wheels.
 */
sealed class DefaultVexWheel : VexWheel, KlaxonVitaminTo {

    sealed class OmniWheel : DefaultVexWheel() {
        /**
         * 2.75" omni (double roller).
         */
        object Omni275 : OmniWheel() {
            override val mass = 0.154.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }

        /**
         * 3.25" omni.
         */
        object Omni325 : OmniWheel() {
            override val mass = 0.132.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }

        /**
         * 4" omni.
         */
        object Omni4 : OmniWheel() {
            override val mass = 0.232.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }
    }

    sealed class TractionWheel : DefaultVexWheel() {
        /**
         * 2.75" traction.
         */
        object Wheel275 : TractionWheel() {
            override val mass = 0.11.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }

        /**
         * 3.25" traction.
         */
        object Wheel325 : TractionWheel() {
            override val mass = 0.11.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }

        /**
         * 4" traction.
         */
        object Wheel4 : TractionWheel() {
            override val mass = 0.198.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }

        /**
         * 5" traction.
         */
        object Wheel5 : TractionWheel() {
            override val mass = 0.352.lbM
            override val centerOfMass =
                CenterOfMass(
                    0.inch,
                    0.inch,
                    0.inch
                )
            @ConvertImmutableMap
            override val specs = emptyImmutableMap<String, Any>()

            override fun toVitamin() = this
        }
    }

    /**
     * 4" high traction.
     */
    object HighTraction : DefaultVexWheel() {
        override val mass = 0.077.lbM
        override val centerOfMass =
            CenterOfMass(0.inch, 0.inch, 0.inch)
        @ConvertImmutableMap
        override val specs = emptyImmutableMap<String, Any>()

        override fun toVitamin() = this
    }

    /**
     * 4" mecanum.
     */
    object Mecanum : DefaultVexWheel() {
        override val mass = 0.41.lbM
        override val centerOfMass =
            CenterOfMass(0.inch, 0.inch, 0.inch)
        @ConvertImmutableMap
        override val specs = emptyImmutableMap<String, Any>()

        override fun toVitamin() = this
    }

    /**
     * 6" wheel leg.
     */
    object WheelLeg : DefaultVexWheel() {
        override val mass = 0.074.lbM
        override val centerOfMass =
            CenterOfMass(0.inch, 0.inch, 0.inch)
        @ConvertImmutableMap
        override val specs = emptyImmutableMap<String, Any>()

        override fun toVitamin() = this
    }
}
