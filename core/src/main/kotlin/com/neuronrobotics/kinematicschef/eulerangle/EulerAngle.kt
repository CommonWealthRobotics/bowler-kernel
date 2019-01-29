/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.eulerangle

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder

/**
 * An Euler angle rotation order. These values taken from "Denavit-Hartenberg Parameterization of
 * Euler Angles" by S. V. Shah, S. K. Saha, and J. K. Dutt.
 *
 * @param order The [RotationOrder].
 * @param preRotation A transform applied before the wrist.
 * @param postRotation A transform applied after the wrist.
 */
sealed class EulerAngle(
    val order: RotationOrder,
    val preRotation: DhParam?,
    val postRotation: DhParam?
) {
    override fun toString() =
        """
            |Order: $order
            |Pre-rotation: $preRotation
            |Post-rotation: $postRotation
        """.trimMargin()
}

object EulerAngleZXZ : EulerAngle(RotationOrder.ZXZ, null, null)

object EulerAngleZYZ : EulerAngle(RotationOrder.ZYZ, null, null)

object EulerAngleZXY : EulerAngle(RotationOrder.ZXY, null, DhParam(0, 0, 0, 90))

object EulerAngleZYX : EulerAngle(RotationOrder.ZYX, null, DhParam(0, -90, 0, -90))

object EulerAngleYXY : EulerAngle(RotationOrder.YXY, null, DhParam(0, 0, 0, 90))

object EulerAngleYZY : EulerAngle(RotationOrder.YZY, null, DhParam(0, 0, 0, 90))

object EulerAngleYXZ : EulerAngle(RotationOrder.YXZ, null, null)

object EulerAngleYZX : EulerAngle(RotationOrder.YZX, null, DhParam(0, -90, 0, -90))

object EulerAngleXYX : EulerAngle(
    RotationOrder.XYX,
    DhParam(0, 90, 0, 0),
    DhParam(0, -90, 0, -90)
)

object EulerAngleXZX : EulerAngle(
    RotationOrder.XZX,
    DhParam(0, 90, 0, 0),
    DhParam(0, -90, 0, -90)
)

object EulerAngleXYZ : EulerAngle(
    RotationOrder.XYZ,
    DhParam(0, 90, 0, 0),
    null
)

object EulerAngleXZY : EulerAngle(
    RotationOrder.XZY,
    DhParam(0, 90, 0, 0),
    DhParam(0, 0, 0, 90)
)
