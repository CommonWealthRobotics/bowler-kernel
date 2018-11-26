package com.neuronrobotics.kinematicschef.solver

import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR

/**
 * Geometric solver (with kinematic decoupling)
 */
class GeometricSolver : AnalyticSolver {
    override fun solve(target: TransformNR, jointSpaceVector: DoubleArray, chain: DHChain): DoubleArray {
        //get list of joint classifications (elbow/pins, prismatic, spherical wrists, others?)

        //decompose spherical wrists into center position points (intersecting axes)

        //geometric solution on position of points of joints

        //greedy compute/selection of angles for each joint to get solution

        return arrayOf(0.0, 0.0, 0.0).toDoubleArray() //TODO: implement this function and return the actual angles
    }

    internal fun wristCenter(wrist: SphericalWrist): Array<Double> {
        return arrayOf(0.0, 0.0, 0.0) //TODO: implement this
    }
}