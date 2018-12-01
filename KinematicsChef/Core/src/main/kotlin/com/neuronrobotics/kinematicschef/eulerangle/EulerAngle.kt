package com.neuronrobotics.kinematicschef.eulerangle

import com.neuronrobotics.kinematicschef.dhparam.DhParam
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder

/**
 * An Euler angle rotation order.
 *
 * @param order The [RotationOrder].
 * @param preRotation A rotation applied before the wrist.
 * @param postRotation A rotation applied after the wrist.
 */
internal sealed class EulerAngle(
    val order: RotationOrder,
    val preRotation: DhParam?,
    val postRotation: DhParam?
)

internal object EulerAngleZXZ : EulerAngle(RotationOrder.ZXZ, null, null)

internal object EulerAngleZYZ : EulerAngle(RotationOrder.ZYZ, null, null)

internal object EulerAngleZXY : EulerAngle(RotationOrder.ZXY, null, DhParam(0, 0, 0, 90))

internal object EulerAngleZYX : EulerAngle(RotationOrder.ZYX, null, DhParam(0, -90, 0, -90))

internal object EulerAngleYXY : EulerAngle(RotationOrder.YXY, null, DhParam(0, 0, 0, 90))

internal object EulerAngleYZY : EulerAngle(RotationOrder.YZY, null, DhParam(0, 0, 0, 90))

internal object EulerAngleYXZ : EulerAngle(RotationOrder.YXZ, null, null)

internal object EulerAngleYZX : EulerAngle(RotationOrder.YZX, null, DhParam(0, -90, 0, -90))

internal object EulerAngleXYX : EulerAngle(
    RotationOrder.XYX,
    DhParam(0, 90, 0, 0),
    DhParam(0, -90, 0, -90)
)

internal object EulerAngleXZX : EulerAngle(
    RotationOrder.XZX,
    DhParam(0, 90, 0, 0),
    DhParam(0, -90, 0, -90)
)

internal object EulerAngleXYZ : EulerAngle(
    RotationOrder.XYZ,
    DhParam(0, 90, 0, 0),
    null
)

internal object EulerAngleXZY : EulerAngle(
    RotationOrder.XZY,
    DhParam(0, 90, 0, 0),
    DhParam(0, 0, 0, 90)
)
