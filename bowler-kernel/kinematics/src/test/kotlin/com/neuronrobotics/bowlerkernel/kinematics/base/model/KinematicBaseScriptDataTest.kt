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
package com.neuronrobotics.bowlerkernel.kinematics.base.model

import arrow.core.Either
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.BodyController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.kinematicBaseScriptData
import com.neuronrobotics.bowlerkernel.kinematics.motion.model.loadClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import java.util.concurrent.TimeUnit

@Timeout(value = 15, unit = TimeUnit.SECONDS)
internal class KinematicBaseScriptDataTest {

    @Test
    fun `test json`() {
        val klaxon = Klaxon()

        val expected = klaxon.kinematicBaseScriptData()

        val decoded = with(KinematicBaseScriptData.encoder()) {
            expected.encode()
        }.decode(KinematicBaseScriptData.decoder())

        assertAll(
            { assertEquals(expected.right(), decoded) },
            {
                assertEquals(
                    NoopBodyController,
                    (decoded as Either.Right).b.bodyController.loadClass<BodyController>(klaxon)
                )
            }
        )
    }
}
