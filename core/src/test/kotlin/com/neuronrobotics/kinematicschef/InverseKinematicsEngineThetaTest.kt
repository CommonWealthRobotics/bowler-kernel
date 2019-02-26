package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.length
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.test.assert

class InverseKinematicsEngineThetaTest {
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
  private val hephaestusParams = TestUtil.hephaestusArmDhParams

  @Test
  fun `test compute d offset` () {
    assert((cmmParams.computeDOffset().length() - 14.00).absoluteValue < 0.001)
    assert((pumaParams.computeDOffset().length() - 14.90).absoluteValue < 0.001)
    assert(hephaestusParams.computeDOffset().length() < 0.001)
  }

  @Test
  fun `test compute cmm theta1` () {
    (0..314).forEach {
      val wristCenter = cmmParams.forwardKinematics(arrayOf(it * 0.01, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray())
      assert (((cmmParams.computeTheta1(wristCenter)[0] - it * 0.01)) < 0.001)
    }
  }

  @Test
  fun `test compute puma theta1` () {
    (0..314).forEach {
      val wristCenter = pumaParams.forwardKinematics(arrayOf(it * 0.01, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray())
      assert (((pumaParams.computeTheta1(wristCenter)[0] - it * 0.01)) < 0.001)
    }
  }

  @Test
  fun `test compute cmm theta 23` () {
    val wristCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(0.0, 0.0, 0.0, 0.0).toDoubleArray())
    val thetas23 = cmmParams.computeTheta23(wristCenter, 0.0)

    val newCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(0.0, thetas23[1][0], thetas23[1][1], 0.0).toDoubleArray())

    val wristCenterVector = wristCenter.cols(3, 4).rows(0, 3)
    val newCenterVector = newCenter.cols(3, 4).rows(0, 3)

    assert((wristCenterVector - newCenterVector).length() < 0.001)

    val newWristCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(-PI/4, 0.0, PI/2, 0.0).toDoubleArray())
    val newTheta1 = cmmParams.computeTheta1(newWristCenter)[0]
    val newThetas23 = cmmParams.computeTheta23(newWristCenter, newTheta1)

    val newNewCenter = cmmParams.subList(0, 4).forwardKinematics(arrayOf(newTheta1, newThetas23[0][0], newThetas23[0][1], 0.0).toDoubleArray())

    val newWristCenterVector = newWristCenter.cols(3, 4).rows(0, 3)
    val newNewCenterVector = newNewCenter.cols(3, 4).rows(0, 3)

    assert((newWristCenterVector - newNewCenterVector).length() < 0.001)
  }

  @Test
  fun `test compute puma theta 23` () {
    val wristCenter = pumaParams.subList(0, 4).forwardKinematics(arrayOf(0.0, 0.0, 0.0, 0.0).toDoubleArray())
    val thetas23 = pumaParams.computeTheta23(wristCenter, 0.0)

    val newCenter = pumaParams.subList(0, 4).forwardKinematics(arrayOf(0.0, thetas23[1][0], thetas23[1][1], 0.0).toDoubleArray())

    val wristCenterVector = wristCenter.cols(3, 4).rows(0, 3)
    val newCenterVector = wristCenter.cols(3, 4).rows(0, 3)

    assert((wristCenterVector - newCenterVector).length() < 0.001)

    val newWristCenter = pumaParams.subList(0, 4).forwardKinematics(arrayOf(PI/4, 0.0, PI/2, 0.0).toDoubleArray())
    val newTheta1 = pumaParams.computeTheta1(newWristCenter)[0]
    val newThetas23 = pumaParams.computeTheta23(newWristCenter, newTheta1)

    val newNewCenter = pumaParams.subList(0, 4).forwardKinematics(arrayOf(newTheta1, 0.0, newThetas23[0][1], 0.0).toDoubleArray())

    val newWristCenterVector = newWristCenter.cols(3, 4).rows(0, 3)
    val newNewCenterVector = newNewCenter.cols(3, 4).rows(0, 3)

    assert((newWristCenterVector - newNewCenterVector).length() < 0.001)
  }
}