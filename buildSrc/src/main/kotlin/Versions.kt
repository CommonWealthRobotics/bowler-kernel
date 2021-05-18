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

/**
 * This file cannot be in a package. That would prevent Gradle from loading it inside `plugins {}`
 * blocks.
 */
object Versions {
    const val projectVersion = "0.11.0"

    const val spotlessPlugin = "5.12.5"
    const val testLoggerPlugin = "3.0.0"
    const val ktlintPlugin = "10.0.0"
    const val dokkaPlugin = "1.4.32"
    const val shadowPlugin = "7.0.0"

    const val kotlin = "1.5.0"
    const val kotlinCoroutines = "1.5.0"
    const val ktlint = "0.41.0"
    const val junit = "5.7.2"
    const val kotest = "4.5.0"
    const val kotestAssertionsArrow = "1.0.2"
    const val logback = "1.2.3"
    const val kotlinLogging = "2.0.6"
    const val mockk = "1.11.0"
    const val arrow = "0.13.2"
    const val githubAPI = "1.115"
    const val jgit = "5.4.0.201906121030-r"
    const val commonsMath3 = "3.6.1"
    const val groovy = "3.0.8"
    const val ivy = "2.5.0"
    const val koin = "3.0.1"
    const val jline = "3.20.0"
    const val bowlerProtoKotlin = "0.8.1"

    const val jacocoTool = "0.8.7"
    const val gradleWrapper = "7.0.2"
}
