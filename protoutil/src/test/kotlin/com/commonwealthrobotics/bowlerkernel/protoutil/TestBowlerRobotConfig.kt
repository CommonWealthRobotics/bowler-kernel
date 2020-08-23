package com.commonwealthrobotics.bowlerkernel.protoutil

import com.commonwealthrobotics.proto.robot_config.Limb
import com.commonwealthrobotics.proto.robot_config.Link
import com.commonwealthrobotics.proto.robot_config.RobotConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestBowlerRobotConfig {
    @Test
    fun `test loading and writing files`(){
        val newRobot =RobotConfig.newBuilder().apply {
            addLimbs(Limb.newBuilder().apply {
                addLinks(Link.newBuilder().apply {
                    name="Link0"
                })
                addLinks(Link.newBuilder().apply {
                    name="Link1"
                })
                addLinks(Link.newBuilder().apply {
                    name="Link2"
                })
            })
        }.build()
        val stringVersion = newRobot.toByteString()
        val loadedRobot = RobotConfig.parseFrom(stringVersion)
        println (stringVersion)
        loadedRobot.shouldBe(newRobot)
    }

}