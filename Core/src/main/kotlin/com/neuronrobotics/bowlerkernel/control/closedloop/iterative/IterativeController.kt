/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop.iterative

import com.neuronrobotics.bowlerkernel.control.closedloop.ClosedLoopController
import com.neuronrobotics.bowlerkernel.util.Limits

/**
 * A closed-loop controller that must be stepped iteratively using [step].
 */
interface IterativeController : ClosedLoopController {

    /**
     * The output limits.
     */
    var outputLimits: Limits<Double>

    /**
     * The minimum time (ms) between iterations.
     */
    var sampleTime: Double

    /**
     * Steps one iteration of the controller.
     *
     * @param reading The new input.
     * @return The new output.
     */
    fun step(reading: Double): Double

    /**
     * Returns the last calculated output.
     *
     * @return The last calculated output.
     */
    fun getOutput(): Double
}
