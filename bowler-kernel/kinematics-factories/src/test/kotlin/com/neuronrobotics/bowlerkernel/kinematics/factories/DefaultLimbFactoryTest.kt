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
package com.neuronrobotics.bowlerkernel.kinematics.factories

import arrow.core.left
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.model.LinkConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbConfigurationData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultLimbFactoryTest {

    private val klaxon = Klaxon().converter(FrameTransformation)

    @Test
    fun `test unequal number of link configs and link scripts`() {
        val factory = DefaultLimbFactory(
            mock {},
            DefaultLinkFactory(mock {}, klaxon),
            klaxon
        )

        assertThrows<IllegalArgumentException> {
            factory.createLimb(
                LimbConfigurationData(
                    "",
                    listOf(
                        LinkConfigurationData(LinkType.Rotary, DhParamData(0, 0, 0, 0))
                    )
                ),
                LimbScriptData(
                    GitFile("", "").left(),
                    GitFile("", "").left(),
                    GitFile("", "").left(),
                    GitFile("", "").left(),
                    GitFile("", "").left(),
                    GitFile("", "").left(),
                    emptyList()
                )
            )
        }
    }
}
