/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.motion.BasicMotionConstraints
import org.junit.jupiter.api.Assertions.assertEquals

internal fun createMotionConstraints(duration: Number) = BasicMotionConstraints(
    duration, 0, 0, 0
)

internal inline fun <reified T> Klaxon.testJsonConversion(input: T) {
    assertEquals(input, parse<T>(toJsonString(input)))
}
