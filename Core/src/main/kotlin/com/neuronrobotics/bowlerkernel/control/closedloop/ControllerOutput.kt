/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

/**
 * An output to a controller.
 */
interface ControllerOutput {

    /**
     * Sets the value of the [output].
     *
     * @param output The new output value.
     */
    fun set(output: Double)
}
