/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion.walking

import com.neuronrobotics.bowlerkernel.control.kinematics.base.KinematicBase

/**
 * A gait is a pattern of movement of the limbs of a [KinematicBase].
 *
 * @param stepLength The distance between contact points from limb n to limb n+1 in meters.
 * @param strideLength The distance between successive contact points of the same limb in meters,
 * typically `[stepLength] * 2`.
 * @param cadence The number of steps per minute.
 */
sealed class Gait(
    open val stepLength: Double,
    open val strideLength: Double,
    open val cadence: Int
) {
    constructor(stepLength: Number, strideLength: Number, cadence: Int) :
        this(stepLength.toDouble(), strideLength.toDouble(), cadence)

    object Stand : Gait(0, 0, 0)

    sealed class Walk(
        override val stepLength: Double,
        override val strideLength: Double,
        override val cadence: Int
    ) : Gait(stepLength, strideLength, cadence)

    data class Jog(
        override val stepLength: Double,
        override val strideLength: Double,
        override val cadence: Int
    ) : Gait(stepLength, strideLength, cadence)

    data class Run(
        override val stepLength: Double,
        override val strideLength: Double,
        override val cadence: Int
    ) : Gait(stepLength, strideLength, cadence)
}
