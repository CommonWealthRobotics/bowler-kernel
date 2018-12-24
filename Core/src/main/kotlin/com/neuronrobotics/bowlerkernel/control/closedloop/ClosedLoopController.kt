/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

/**
 * A closed-loop controller.
 */
interface ClosedLoopController {

    /**
     * Sets a new [target].
     *
     * @param target The new target.
     */
    fun setTarget(target: Double)

    /**
     * Returns the last set target.
     *
     * @return The last set target.
     */
    fun getTarget(): Double

    /**
     * Returns the last calculated error.
     *
     * @return The last calculated error.
     */
    fun getError(): Double

    /**
     * Whether the controller has settled. Determining what settling means is
     * implementation-dependent. If the controller is disabled, this returns true.
     *
     * @return Whether the controller has settled.
     */
    fun isSettled(): Boolean

    /**
     * Resets the controller's internal state so it is similar to when it was first initialized,
     * while keeping any user-configured information.
     */
    fun reset()

    /**
     * Sets whether the controller is disabled.
     */
    fun setDisabled(disabled: Boolean)

    /**
     * Whether the controller is disabled. A disabled controller cannot write any output to what
     * it controls. Disabling the controller will cause it to write a disabled state to its output.
     * Enabling the controller will cause it to move to its last set target, unless it was reset
     * before being enabled.
     *
     * @return Whether the controller is disabled.
     */
    fun isDisabled(): Boolean
}
