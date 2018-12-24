/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.limb

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.model.DhParamData
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.control.kinematics.limb.model.LinkData
import com.neuronrobotics.bowlerkernel.control.testJsonConversion
import com.neuronrobotics.bowlerkernel.util.Limits
import org.junit.jupiter.api.Test

internal class LimbDataTest {

    @Test
    fun `basic test`() {
        Klaxon().testJsonConversion(
            LimbData(
                SimpleLimbId("id"),
                listOf(
                    LinkData(
                        LinkType.Rotary,
                        DhParamData(
                            0.0,
                            0.0,
                            0.0,
                            0.0
                        ),
                        Limits(1.0, 0.0)
                    )
                ),
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h"
            )
        )
    }
}
