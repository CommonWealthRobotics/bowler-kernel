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
package com.neuronrobotics.bowlerkernel.gitfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.SECONDS)
internal class GitHubFSTest {

    @ParameterizedTest
    @MethodSource("isRepoUrlSource")
    fun `test isRepoUrl`(data: Pair<String, Boolean>) {
        assertEquals(data.second, GitHubFS.isRepoUrl(data.first))
    }

    @ParameterizedTest
    @MethodSource("isGistUrlSource")
    fun `test isGistUrl`(data: Pair<String, Boolean>) {
        assertEquals(data.second, GitHubFS.isGistUrl(data.first))
    }

    @ParameterizedTest
    @MethodSource("stripUrlCharactersFromGitUrlSource")
    fun `test stripUrlCharactersFromGitUrl`(data: Pair<String, String>) {
        assertEquals(data.second, GitHubFS.stripUrlCharactersFromGitUrl(data.first))
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun isRepoUrlSource() = listOf(
            "" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to true,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to true,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to true,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to true,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to false,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to false,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to false,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to false
        )

        @Suppress("unused")
        @JvmStatic
        fun isGistUrlSource() = listOf(
            "" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to false,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to false,
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to false,
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to false,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to true,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to true,
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to true,
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to true
        )

        @Suppress("unused")
        @JvmStatic
        fun stripUrlCharactersFromGitUrlSource() = listOf(
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                "CommonWealthRobotics/BowlerBuilder",
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git" to
                "CommonWealthRobotics/BowlerBuilder",
            "https://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                "CommonWealthRobotics/BowlerBuilder",
            "http://github.com/CommonWealthRobotics/BowlerBuilder.git/" to
                "CommonWealthRobotics/BowlerBuilder",
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                "5681d11165708c3aec1ed5cf8cf38238",
            "http://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git/" to
                "5681d11165708c3aec1ed5cf8cf38238"
        )
    }
}
