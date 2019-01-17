/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.protocol

/**
 * An error which is caused by a timeout.
 *
 * @param timeoutDuration The duration, in ms, of the timeout.
 */
data class TimeoutError(
    val timeoutDuration: Long = 100
)
