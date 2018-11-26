package com.neuronrobotics.kinematicschef.solver

import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import org.junit.jupiter.api.Test

internal class AnalyticSolverTest {
    @Test
    fun `test wrist center`() {
        val wrist = SphericalWrist(TestUtil.randomDhParamList(3))

        GeometricSolver().wristCenter(wrist)
    }
}