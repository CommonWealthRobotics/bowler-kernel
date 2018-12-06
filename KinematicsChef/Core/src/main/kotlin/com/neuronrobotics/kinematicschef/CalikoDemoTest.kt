package com.neuronrobotics.kinematicschef

import au.edu.federation.caliko.FabrikBone3D
import au.edu.federation.caliko.FabrikChain3D
import au.edu.federation.caliko.FabrikJoint3D
import au.edu.federation.caliko.FabrikStructure3D
import au.edu.federation.caliko.demo3d.CalikoDemoStructure3D
import au.edu.federation.utils.Mat4f
import au.edu.federation.utils.Vec3f
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.toDhParams
import com.neuronrobotics.kinematicschef.util.getFrameTranslationMatrix
import com.neuronrobotics.kinematicschef.util.getRotation
import com.neuronrobotics.kinematicschef.util.getRotationMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.ejml.simple.SimpleMatrix
import kotlin.math.abs

class CalikoDemoTest : CalikoDemoStructure3D() {

    val jointSpaceVector: DoubleArray
    val chain: DHChain

    init {
        jointSpaceVector = listOf(0.0, 0.0, 0.0).toDoubleArray()

        val mockChain = DHChain(object : AbstractKinematicsNR() {
            override fun forwardKinematics(p0: DoubleArray?): TransformNR {
                TODO("not implemented")
            }

            override fun disconnectDevice() {
                TODO("not implemented")
            }

            override fun inverseKinematics(p0: TransformNR?): DoubleArray {
                TODO("not implemented")
            }

            override fun connectDevice(): Boolean {
                TODO("not implemented")
            }
        })

//        mockChain.addLink(DHLink(0.0, 0.0, 10.0, 0.0))

//        mockChain.addLink(DHLink(0.0, 0.0, 10.0, 0.0))
//        mockChain.addLink(DHLink(0.0, 0.0, 10.0, 0.0))

        mockChain.addLink(DHLink(135.0, 0.0, 0.0, -90.0))
        mockChain.addLink(DHLink(0.0, 0.0, 175.0, 0.0))
        mockChain.addLink(DHLink(0.0, 90.0, 169.28, 0.0))

        chain = mockChain
    }

    override fun setup() {
        require(jointSpaceVector.size == chain.links.size) {
            """
                |The joint angles and DH params must have equal size.
                |Number of joint angles: ${jointSpaceVector.size}
                |Number of DH params: ${chain.links.size}
            """.trimMargin()
        }

        val fabrikChain = FabrikChain3D().apply {
            setFixedBaseMode(true)
        }

        var previousLinkRotationAxis = Vec3f()
        val dhParams = chain.toDhParams()
        dhParams.forEachIndexed { index, dhParam ->
            val boneLength: Float = calculateLinkLength(dhParam)/10

            if (index == 0) {
                // The first link can't be added using addConsecutiveBone()
                fabrikChain.addBone(
                    FabrikBone3D(
                        Vec3f(0.0f),
                        X_AXIS.mult3(
                            dhParam.frameTransformation.getRotation()
                        ).times(boneLength)
                    )
                )

                previousLinkRotationAxis = Z_AXIS.mult3(
                    dhParam.frameTransformation.getRotation()
                )
                fabrikChain.setGlobalHingedBasebone(
                    previousLinkRotationAxis,
                    chain.getlowerLimits()?.get(index)?.toFloat() ?: 180.0f,
                    chain.upperLimits?.get(index)?.toFloat() ?: 180.0f,
                    previousLinkRotationAxis.mult3(
                        getRotationMatrix(90, 0, 0)
                    )
                )
            } else {
                // TODO: The directionUV could be X or Z depending on if we need to use d or r
                // TODO: Pull hardware limits from the DHChain
                fabrikChain.addConsecutiveHingedBone(
                    X_AXIS.mult3(
                        dhParam.frameTransformation.getRotation()
                    ),
                    boneLength,
                    FabrikJoint3D.JointType.LOCAL_HINGE,
                    previousLinkRotationAxis.mult3(
                        dhParam.frameTransformation.getRotation()
                    ),
                    chain.getlowerLimits()?.get(index)?.toFloat() ?: 180.0f,
                    chain.upperLimits?.get(index)?.toFloat() ?: 180.0f,
                    previousLinkRotationAxis.mult3(
                        dhParam.frameTransformation.getRotation()
                    ).mult3(
                        getRotationMatrix(90, 0, 0)
                    )
                )
            }
        }

        structure = FabrikStructure3D("Demo 13 - KinematicsChef Demo")
        structure.addChain(fabrikChain)
    }

    override fun drawTarget(mvpMatrix: Mat4f) {
    }

    /**
     * Calculates the length of a link from its [DhParam].
     */
    private fun calculateLinkLength(dhParam: DhParam) =
        dhParam.length.toFloat().also {
            return when {
                abs(it) < defaultBoneLengthDelta -> defaultBoneLength
                else -> abs(it)
            }
        }

    private val defaultBoneLength = 10.0f
    private val defaultBoneLengthDelta = 1e-6
    private val baseUnitVector = Vec3f(0.0f, 0.0f, 1.0f).normalise()
    private val Z_AXIS = Vec3f(0.0f, 1.0f, 0.0f)
    private val Y_AXIS = Vec3f(1.0f, 0.0f, 0.0f)
    private val X_AXIS = Vec3f(0.0f, 0.0f, -1.0f)

    private fun Vec3f.mult4(ft: SimpleMatrix): Vec3f {
        val vecAsMat = getFrameTranslationMatrix(x, y, z)
        val result = vecAsMat.mult(ft).getTranslation()
        println("mult4======")
        vecAsMat.print()
        ft.print()
        return Vec3f(result[0].toFloat(), result[1].toFloat(), result[2].toFloat()).normalise()
    }

    private fun Vec3f.mult3(ft: SimpleMatrix): Vec3f {
        val vecAsMat = SimpleMatrix(3, 1).apply {
            this[0, 0] = x.toDouble()
            this[1, 0] = y.toDouble()
            this[2, 0] = z.toDouble()
        }//getFrameTranslationMatrix(x, y, z)
        println("mult3======")
        vecAsMat.print()
        ft.print()
        val result = ft.mult(vecAsMat)//.getTranslation()
        return Vec3f(
            result[0, 0].toFloat(),
            result[1, 0].toFloat(),
            result[2, 0].toFloat()
        ).normalise()
    }

    private fun FabrikChain3D.solveForTarget(x: Number, y: Number, z: Number) =
        solveForTarget(x.toFloat(), y.toFloat(), z.toFloat())

    private fun Double.modulus(rhs: Double) = (rem(rhs) + rhs).rem(rhs)
}
