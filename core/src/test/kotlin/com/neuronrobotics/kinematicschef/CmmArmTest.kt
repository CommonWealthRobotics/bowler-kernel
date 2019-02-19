package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.MobileBaseLoader
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.dhparam.toFrameTransformation
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.length
import com.neuronrobotics.kinematicschef.util.toTransformNR
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.math.absoluteValue

class CmmArmTest {
  @Test
  fun `test compute d offset` () {
    val cmmInputArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
    ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

    val cmmChain = cmmInputArm.base.appendages[0].chain
    val cmmParams = cmmChain.toDhParams()

    val engine = InverseKinematicsEngine.getInstance()

    assert((engine.computeDOffset(cmmParams).length() - 14.00).absoluteValue < 0.001)

    val pumaArm = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/NotOctogonapus/c3fc39308a506d4cb1cd7297193c41e7",
            "InputArmBase_copy.xml",
            null
    ) as? MobileBaseLoader ?: fail { "The script did not return a MobileBaseLoader." }

    val pumaChain = pumaArm.base.appendages[1].chain
    val pumaParams = pumaChain.toDhParams()

    assert((engine.computeDOffset(pumaParams).length() - 14.90).absoluteValue < 0.001)
  }
}