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
object Metadata {
    const val projectName = "bowler-kernel"
    const val projectDescription = "The heart of the Bowler stack."
    const val organization = "commonwealthrobotics"
    const val license = "LGPL-3.0"

    object Bintray {
        const val repo = "maven-artifacts"
        const val vcsUrl = "https://github.com/CommonWealthRobotics/bowler-kernel.git"
        const val githubRepo = "https://github.com/CommonWealthRobotics/bowler-kernel"
    }
}
