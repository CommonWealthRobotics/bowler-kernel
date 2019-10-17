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
@file:SuppressWarnings("LongMethod", "TooManyFunctions")
@file:Suppress("UnstableApiUsage")

package com.neuronrobotics.bowlerkernel.kinematics

import com.beust.klaxon.Klaxon
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.BasicMotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.octogonapus.ktguava.collections.immutableListOf

internal fun createMotionConstraints(duration: Number) = BasicMotionConstraints(
    duration, 0, 0, 0
)

internal inline fun <reified T> Klaxon.testJsonConversion(input: T) {
    assertEquals(input, parse<T>(toJsonString(input).also { println(it) }))
}

internal val seaArmLinks: ImmutableList<Link> = immutableListOf(
    Link(
        LinkType.Rotary,
        DhParam(135, 0, 0, -90),
        NoopInertialStateEstimator
    ),
    Link(
        LinkType.Rotary,
        DhParam(0, 0, 175, 0),
        NoopInertialStateEstimator
    ),
    Link(
        LinkType.Rotary,
        DhParam(0, 90, 169.28, 0),
        NoopInertialStateEstimator
    )
)

internal fun randomFrameTransformation() = Random.Default.randomFrameTransformation()

internal fun Random.randomFrameTransformation() =
    FrameTransformation.fromTranslation(nextDouble(10.0), nextDouble(10.0), nextDouble(10.0)) *
        FrameTransformation.fromRotation(nextDouble(10.0), nextDouble(10.0), nextDouble(10.0))
