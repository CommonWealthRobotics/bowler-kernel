/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

/**
 * An input to a controller.
 */
interface ControllerInput {

    /**
     * Return the latest input value.
     *
     * @return The input value.
     */
    fun get(): Double
}
