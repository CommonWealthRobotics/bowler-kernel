/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop.iterative

import com.neuronrobotics.bowlerkernel.control.closedloop.util.SettledUtil
import com.neuronrobotics.bowlerkernel.control.closedloop.util.Timer
import com.neuronrobotics.bowlerkernel.util.Limits
import kotlin.math.abs
import kotlin.math.sign

/**
 * An iterative position PID controller.
 */
class IterativePosPIDController(
    private val kP: Double,
    private val kI: Double,
    private val kD: Double,
    private val kBias: Double,
    private val settledUtil: SettledUtil
) : IterativeController {

    private val timer = Timer()
    private var output = 0.0
    private var target = 0.0
    private var lastReading = 0.0
    private var disabled = false
    private var error = 0.0
    private var lastError = 0.0
    private var integral = 0.0
    private var derivative = 0.0

    override var outputLimits = Limits(1.0, -1.0)
    override var sampleTime = 10.0
    var errorSumLimits = Limits(Double.MAX_VALUE, 0.0)
    var integralLimits = Limits(1.0, -1.0)

    /**
     * Whether to reset the integral term when the error crosses 0.
     */
    var shouldResetIntegralOnErrorCross = true

    override fun step(reading: Double): Double {
        lastReading = reading

        if (disabled) {
            return 0.0
        } else {
            timer.placeHardMark()

            if (timer.getDtFromHarkMark() >= sampleTime) {
                error = getError()

                if (
                    (abs(error) < target - errorSumLimits.minimum &&
                        abs(error) > target - errorSumLimits.maximum) ||
                    (abs(error) > target + errorSumLimits.minimum &&
                        abs(error) < target + errorSumLimits.maximum)
                ) {
                    integral += kI * error
                }

                if (shouldResetIntegralOnErrorCross && sign(error) != sign(lastError)) {
                    integral = 0.0
                }

                integral = clamp(integral, integralLimits)
                derivative = reading - lastReading // TODO: Filter

                output = clamp(kP * error + integral - kD * derivative + kBias, outputLimits)

                lastError = error
                timer.clearHardMark()
                settledUtil.isSettled(error)
            }
        }

        return output
    }

    override fun getOutput() = if (isDisabled()) 0.0 else output

    override fun setTarget(target: Double) {
        this.target = target
    }

    override fun getTarget() = target

    override fun getError() = target - lastReading

    override fun isSettled() = if (isDisabled()) true else settledUtil.isSettled(error)

    override fun reset() {
        error = 0.0
        lastError = 0.0
        lastReading = 0.0
        integral = 0.0
        output = 0.0
        settledUtil.resetTime()
    }

    override fun setDisabled(disabled: Boolean) {
        this.disabled = disabled
    }

    override fun isDisabled() = disabled

    private fun clamp(value: Double, limits: Limits<Double>) =
        clamp(value, limits.minimum, limits.maximum)

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return when {
            value > max -> max
            value < min -> min
            else -> value
        }
    }
}
