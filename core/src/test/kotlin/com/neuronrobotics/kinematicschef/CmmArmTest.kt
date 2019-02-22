package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.length
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.math.absoluteValue

class CmmArmTest {
  private val cmmInputArm = ScriptingEngine.gitScriptRun(
          "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
          "InputArmBase_copy.xml",
          null
  ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

  private val cmmChain = cmmInputArm.base.appendages[0].chain
  private val cmmParams = cmmChain.toDhParams()

  private val pumaArm = ScriptingEngine.gitScriptRun(
          "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
          "InputArmBase_copy.xml",
          null
  ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

  private val pumaChain = pumaArm.base.appendages[1].chain
  private val pumaParams = pumaChain.toDhParams()

  @Test
  fun `test compute d offset` () {
    assert((cmmParams.computeDOffset().length() - 14.00).absoluteValue < 0.001)
    assert((pumaParams.computeDOffset().length() - 14.90).absoluteValue < 0.001)
  }

  @Test
  fun `test compute theta1` () {
    (0..314).forEach {
      val wristCenter = cmmParams.forwardKinematics(arrayOf(it * 0.01, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray())
      assert (((cmmParams.computeTheta1(wristCenter)[0] - it * 0.01)) < 0.001)
    }

    (0..314).forEach {
      val wristCenter = pumaParams.forwardKinematics(arrayOf(it * 0.01, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray())
      assert (((pumaParams.computeTheta1(wristCenter)[0] - it * 0.01)) < 0.001)
    }
  }

  @Test
  fun `test compute theta 23` () {
    val wristCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(0.0, 0.0, 0.0, 0.0).toDoubleArray())
    val theta23 = cmmParams.computeTheta23(wristCenter, 0.0)

    val newCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(0.0, theta23[0][0], theta23[0][1], 0.0).toDoubleArray())
  }
}