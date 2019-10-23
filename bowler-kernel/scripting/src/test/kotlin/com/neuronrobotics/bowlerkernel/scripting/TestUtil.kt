/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.effects.IO
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock

// This can't be internal because it is needed in a script
fun mockBowlerRPCProtocol(): BowlerRPCProtocol {
    return mock {
        on { connect() } doReturn IO.just(Unit)
        on { disconnect() } doReturn IO.just(Unit)
        on { isResourceInRange(any()) } doReturn IO.just(true)
    }
}
