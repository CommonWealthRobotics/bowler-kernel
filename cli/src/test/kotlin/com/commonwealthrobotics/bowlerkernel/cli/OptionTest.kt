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
package com.commonwealthrobotics.bowlerkernel.cli

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.InetAddress

internal class OptionTest {

    @Test
    fun `match no arg`() {
        val option = option<Int>(short = "a", long = "aa", help = "")
        option.matchAndRemove(mutableListOf()).shouldBeNull()
    }

    @Test
    fun `match the short switch`() {
        val option = option<Int>(short = "a", long = "aa", help = "")
        val args = mutableListOf("-a", "1")
        option.matchAndRemove(args).shouldBe(1)
        args.shouldBeEmpty()
    }

    @Test
    fun `match the long switch`() {
        val option = option<Int>(short = "a", long = "aa", help = "")
        val args = mutableListOf("--aa", "1")
        option.matchAndRemove(args).shouldBe(1)
        args.shouldBeEmpty()
    }

    @Test
    fun `don't match a different switch`() {
        val option = option<Int>(short = "a", long = "aa", help = "")
        val args = mutableListOf("--ab", "1")
        option.matchAndRemove(args).shouldBeNull()
        args.shouldContainExactly("--ab", "1")
    }

    @Test
    fun `match a short switch but fail parsing`() {
        val option = option<Int>(short = "a", long = "aa", help = "")
        val args = mutableListOf("-a", "q")
        shouldThrow<NumberFormatException> {
            option.matchAndRemove(args)
        }
    }

    @Test
    fun `parse ipv4`() {
        Option.parse(InetAddress::class, "192.168.1.177").shouldBe(
            InetAddress.getByAddress(
                byteArrayOf(
                    192.toByte(),
                    168.toByte(),
                    1.toByte(),
                    177.toByte()
                )
            )
        )
    }
}
