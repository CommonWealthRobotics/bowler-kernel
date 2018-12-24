/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop.async

import com.neuronrobotics.bowlerkernel.control.closedloop.ClosedLoopController

/**
 * A closed-loop controller that steps itself and writes to its output.
 */
interface AsyncController : ClosedLoopController {

    /**
     * Blocks the calling thread until the controller has settled.
     */
    fun blockUntilSettled()
}
