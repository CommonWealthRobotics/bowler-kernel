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

import io.kotest.matchers.longs.shouldBeZero
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

internal class CommandTest {

    @Nested
    inner class TerminalCommands {
        @Test
        fun `invoke a terminal command with no options`() {
            val latch = CountDownLatch(1)
            val expectedPrintout = "output"
            val cmd = Command(name = "foo", help = "") { latch.countDown(); expectedPrintout }
            val args = mutableListOf<String>()
            cmd(args).shouldContain(expectedPrintout)
            latch.count.shouldBeZero()
        }

        @Test
        fun `invoke a terminal command with one non-required option`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = false
                    )
                )
            ) {
                it.option<Int>("aa").shouldBe(1)
                latch.countDown()
                ""
            }
            val args = mutableListOf("-a", "1")
            cmd(args)
            latch.count.shouldBeZero()
        }

        @Test
        fun `invoke a terminal command with one required option`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = true
                    )
                )
            ) {
                it.option<Int>("aa").shouldBe(1)
                latch.countDown()
                ""
            }
            val args = mutableListOf("-a", "1")
            cmd(args)
            latch.count.shouldBeZero()
        }

        @Test
        fun `invoke a terminal command with one missing required option`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = true
                    )
                )
            ) {
                latch.countDown()
                ""
            }
            val args = mutableListOf<String>()
            cmd(args).apply {
                shouldContain("Missing option")
                shouldContain("aa")
            }
            latch.count.shouldBe(1)
        }

        @Test
        fun `invoke a terminal command with one missing non-required option`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = false
                    )
                )
            ) {
                it.option("aa", 2).shouldBe(2)
                latch.countDown()
                ""
            }
            val args = mutableListOf<String>()
            cmd(args)
            latch.count.shouldBe(0)
        }

        @Test
        fun `invoke a terminal command with one option with a missing value`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = false
                    )
                )
            ) {
                latch.countDown()
                ""
            }
            val args = mutableListOf("-a") // Don't pass a value
            cmd(args)
            latch.count.shouldBe(1)
        }

        @Test
        fun `invoke a terminal command with one option with an invalid value`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = false,
                        validator = { false }
                    )
                )
            ) {
                latch.countDown()
                ""
            }
            val args = mutableListOf("-a", "1")
            cmd(args).apply {
                shouldContain("Invalid option")
                shouldContain("aa")
            }
            latch.count.shouldBe(1)
        }

        @Test
        fun `invoke a terminal command with one option that fails to parse`() {
            val latch = CountDownLatch(1)
            val cmd = Command(
                name = "foo",
                help = "bar",
                options = listOf(
                    option<Int>(
                        short = "a",
                        long = "aa",
                        help = "",
                        required = false,
                        validator = { false }
                    )
                )
            ) {
                latch.countDown()
                ""
            }
            val args = mutableListOf("-a", "q")
            cmd(args).apply {
                shouldContain("Invalid option")
                shouldContain("aa")
            }
            latch.count.shouldBe(1)
        }

        @Test
        fun `passing an unknown option is an error`() {
            val cmd = Command(name = "foo", help = "") { "" }
            val args = mutableListOf("option1")
            cmd(args).apply {
                shouldContain("Unknown option")
                args.forEach { shouldContain(it) }
            }
        }
    }

    @Nested
    inner class NonTerminalCommands {
        @Test
        fun `run a non-terminal command`() {
            val latch = CountDownLatch(1)
            val expectedPrintout = "output"
            val cmd = Command(
                name = "foo",
                children = listOf(
                    Command(name = "inner1", help = "") { "" },
                    Command(name = "inner2", help = "") { latch.countDown(); expectedPrintout },
                    Command(name = "inner3", help = "") { "" },
                )
            )
            val args = mutableListOf("inner2")
            cmd(args).shouldContain(expectedPrintout)
            latch.count.shouldBeZero()
        }
    }
}
