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
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Test

internal class AnalyticSolverTest {
    private val wristIdentifier = DefaultWristIdentifier()
    private val chainIdentifier = DefaultChainIdentifier(wristIdentifier)
    private val dhClassifier = DefaultDhClassifier(wristIdentifier)

    @Test
    fun `test wrist solve`() {
        val chain = immutableListOf(
                DhParam(1.0,0.0,0.0,0.0),
                DhParam(0.0,0.0,1.0,-90.0),
                DhParam(1.0,0.0,0.0,90.0)
        )

        val target : TransformNR = TransformNR().setX(1.0).setY(0.0).setZ(1.0) //target point
        val jointSpaceVector = doubleArrayOf(0.0, 0.0, 0.0) //initial joint angles, will play with these later

        val chainElements = chainIdentifier.identifyChain(chain)
        val eulerAngles = chainElements
                .mapNotNull { it as? SphericalWrist }
                .map { it to dhClassifier.deriveEulerAngles(it) }
                .toImmutableMap()

        //validateEulerAngles(eulerAngles)

        //TODO: solver alg
    }
}