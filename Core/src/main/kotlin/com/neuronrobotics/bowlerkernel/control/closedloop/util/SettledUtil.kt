/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop.util

import kotlin.math.abs

/**
 * A utility to determine if a controller has settled based on error. A control loop is settled
 * if the error is within [atTargetError] and [atTargetDerivative] for [atTargetTime].
 *
 * @param atTargetError The minimum error to be considered settled.
 * @param atTargetDerivative The minimum error derivative to be considered settled.
 * @param atTargetTime The minimum time within [atTargetError] and [atTargetDerivative] to be
 * considered settled.
 */
class SettledUtil(
    private val atTargetError: Double = 50.0,
    private val atTargetDerivative: Double = 5.0,
    private val atTargetTime: Long = 250
) {

    private val timer = Timer()
    private var lastError = 0.0

    /**
     * Calculates whether the controller is settled.
     *
     * @param error The current error.
     * @return Whether the controller is settled.
     */
    fun isSettled(error: Double): Boolean {
        if (abs(error) <= atTargetError && abs(error - lastError) <= atTargetDerivative) {
            /**
             * [Timer.getDtFromHarkMark] returns 0 if there is no hard mark set, so this needs to
             * be special-cased. Setting [atTargetTime] to 0 means that the user wants to exit
             * immediately when in range of the target.
             */
            if (atTargetTime == 0L) {
                return true
            }

            timer.placeHardMark()
        } else {
            timer.clearHardMark()
        }

        lastError = error
        return timer.getDtFromHarkMark() > atTargetTime
    }

    /**
     * Resets the [atTargetTime] timer.
     */
    fun resetTime() {
        timer.clearHardMark()
        lastError = 0.0
    }
}
