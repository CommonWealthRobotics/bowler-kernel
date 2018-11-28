package com.neuronrobotics.kinematicschef.solver

import com.neuronrobotics.kinematicschef.InverseKinematicsEngine
import com.neuronrobotics.kinematicschef.TestUtil
import com.neuronrobotics.kinematicschef.classifier.DefaultChainIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultDhClassifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifier
import com.neuronrobotics.kinematicschef.classifier.DefaultWristIdentifierTest
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.immutableListOf
import com.neuronrobotics.kinematicschef.util.toImmutableMap
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AnalyticSolverTest {
    private val wristIdentifier = DefaultWristIdentifier()
    private val ikEngine = InverseKinematicsEngine(
            DefaultChainIdentifier(wristIdentifier),
            DefaultDhClassifier(wristIdentifier)
    )

    @Test
    fun `test wrist center`() {
        val wrist = SphericalWrist(immutableListOf(
                DhParam(1.0,0.0,0.0,0.0),
                DhParam(0.0,0.0,1.0,-90.0),
                DhParam(1.0,0.0,0.0,90.0)
        ))

        //target frame transformation
        val target = TransformNR(
        ).setX(2.0).setY(0.0).setZ(1.0)
        target.rotation = RotationNR(-90.0, 0.0, 0.0)

        val wristCenter = ikEngine.wristCenter(target, wrist)
        assertEquals(2.0, wristCenter.x)
        assertEquals(-2.0, wristCenter.y)
        assertEquals(1.0, wristCenter.z)
    }
}