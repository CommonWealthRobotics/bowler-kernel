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
import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.ejml.simple.SimpleMatrix
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
        val target = SimpleMatrix(4, 4)

        val rotationMatrix = Rotation(
            RotationOrder.ZYX,
            RotationConvention.FRAME_TRANSFORM,
            0.0,
            0.0,
            Math.PI * 0.5
        ).matrix
        target.setRow(0, 0, *(rotationMatrix[0] + 2.0))
        target.setRow(1, 0, *(rotationMatrix[1] + 0.0))
        target.setRow(2, 0, *(rotationMatrix[2] + 1.0))
        target[3, 3] = 1.0

        val wristCenter = ikEngine.wristCenter(target, wrist)
        assert(Math.abs(2.0 - wristCenter.x) < 0.00001)
        assert(Math.abs(-2.0 - wristCenter.y) < 0.00001)
        assert(Math.abs(1.0 - wristCenter.z) < 0.00001)
    }
}