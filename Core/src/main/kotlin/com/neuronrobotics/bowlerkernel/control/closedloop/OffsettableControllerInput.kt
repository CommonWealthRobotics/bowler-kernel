/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

class OffsettableControllerInput(
    private val input: ControllerInput
) : ControllerInput {

    private var offset = 0.0

    override fun get(): Double {
        return input.get() - offset
    }

    /**
     * Sets the "absolute" zero position of this input to its current position.
     */
    fun tare() {
        offset = input.get()
    }
}
