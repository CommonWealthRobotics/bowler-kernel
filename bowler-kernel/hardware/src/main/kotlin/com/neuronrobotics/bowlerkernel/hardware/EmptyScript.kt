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
package com.neuronrobotics.bowlerkernel.hardware

import arrow.core.right
import com.google.common.collect.ImmutableList
import org.octogonapus.ktguava.collections.emptyImmutableList

/**
 * A script which does nothing. Can be used to maintain a hardware registry between child scripts.
 */
class EmptyScript : Script() {

    init {
        startScript(emptyImmutableList())
    }

    override fun runScript(args: ImmutableList<Any?>) = Unit.right()

    @SuppressWarnings("EmptyFunctionBlock")
    override fun stopScript() {
    }
}
