/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.base

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.control.kinematics.createMockKinematicBaseData
import com.neuronrobotics.bowlerkernel.control.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.control.testJsonConversion
import org.junit.jupiter.api.Test

internal class KinematicBaseDataTest {

    @Test
    fun `test json conversion`() {
        Klaxon().converter(FrameTransformation.converter)
            .testJsonConversion(createMockKinematicBaseData())
    }
}
