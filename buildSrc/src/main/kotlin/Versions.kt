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
    const val bowlerKernel = "0.1.0"

    const val spotlessPlugin = "3.29.0"
    const val testLoggerPlugin = "2.0.0"
    const val ktlintPlugin = "9.3.0"
    const val detektPlugin = "1.9.0"

    const val kotlin = "1.3.72"
    const val ktlint = "0.37.2"
    const val junit = "5.6.+"
    const val kotest = "4.1.+"
    const val slf4j_log4j12 = "1.7.+"
    const val apacheLog4jExtras = "1.2.+"
    const val kotlinLogging = "1.7.+"
    const val mockk = "1.10.+"
    const val arrow = "0.10.+"
    const val githubAPI = "1.115"
    const val jgit = "5.4.0.201906121030-r"
    const val commonsMath3 = "3.6.+"
    const val ktGuavaCore = "0.2.3"
    const val protobufJava = "3.12.4"
    const val grpcAll = "1.31.0"
    const val javaxAnnotationAPI = "1.3.2"

    const val jacocoTool = "0.8.5"
    const val gradleWrapper = "6.5.1"
}
