/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion

/**
 * Estimates the [InertialState] for something.
 */
interface InertialStateEstimator {

    /**
     * Computes a new estimate for the state.
     *
     * @return An estimated [InertialState].
     */
    fun getInertialState(): InertialState
}
