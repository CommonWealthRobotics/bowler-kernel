/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OffsettableControllerInputTest {

    private val mockInput = object : ControllerInput {
        var reading = 0.0
        override fun get() = reading
    }

    private val input = OffsettableControllerInput(mockInput)

    @Test
    fun `test tare position`() {
        mockInput.reading = 10.0
        assertEquals(10.0, input.get())

        input.tare()
        assertEquals(0.0, input.get())

        mockInput.reading = 20.0
        assertEquals(10.0, input.get())
    }
}
