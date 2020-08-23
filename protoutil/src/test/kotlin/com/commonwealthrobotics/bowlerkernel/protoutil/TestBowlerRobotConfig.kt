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
package com.commonwealthrobotics.bowlerkernel.protoutil

import com.commonwealthrobotics.proto.robot_config.Limb
import com.commonwealthrobotics.proto.robot_config.Link
import com.commonwealthrobotics.proto.robot_config.RobotConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestBowlerRobotConfig {
    @Test
    fun `test loading and writing files`() {
        val newRobot = RobotConfig.newBuilder().apply {
            addLimbs(
                Limb.newBuilder().apply {
                    addLinks(
                        Link.newBuilder().apply {
                            name = "Link0"
                        }
                    )
                    addLinks(
                        Link.newBuilder().apply {
                            name = "Link1"
                        }
                    )
                    addLinks(
                        Link.newBuilder().apply {
                            name = "Link2"
                        }
                    )
                }
            )
        }.build()
        val stringVersion = newRobot.toByteString()
        val loadedRobot = RobotConfig.parseFrom(stringVersion)
        println(stringVersion)
        loadedRobot.shouldBe(newRobot)
    }
}
