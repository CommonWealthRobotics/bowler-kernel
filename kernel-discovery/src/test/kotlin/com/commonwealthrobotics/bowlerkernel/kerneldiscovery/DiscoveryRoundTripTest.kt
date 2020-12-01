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
package com.commonwealthrobotics.bowlerkernel.kerneldiscovery

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test

internal class DiscoveryRoundTripTest {

    @Test
    fun `scan the local host with no servers running`() {
        NameClient.scan().shouldBeEmpty()
    }

    @Test
    fun `scan the local host with one server running`() {
        val name = "kernel"

        val ns = NameServer(name)
        ns.ensureStarted()
        ns.name.shouldBe(name)
        while (!ns.isRunning.get()) { Thread.sleep(10) }
        NameClient.scan().map { it.a }.shouldContainExactly(name)

        ns.ensureStopped()
        ns.isRunning.get().shouldBeFalse()
    }

    @Test
    fun `determine unique name`() {
        val name = "kernel"
        val uniqueName = NameServer.determineUniqueName(name, listOf(name))
        uniqueName.shouldStartWith(name)
        uniqueName.shouldNotBeEqualComparingTo(name)
    }
}
