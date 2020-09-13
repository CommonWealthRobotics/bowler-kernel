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
package com.commonwealthrobotics.bowlerkernel.util

import java.nio.file.Path
import java.nio.file.Paths

/**
 * The main directory Bowler applications work in.
 */
const val BOWLER_DIRECTORY = ".bowler"

/**
 * The kernel's directory.
 */
const val BOWLERKERNEL_DIRECTORY = "kernel"

/**
 * The GitFS cache.
 */
const val GIT_CACHE_DIRECTORY = "git-cache"

/**
 * The GitHub implementation of GitFS cache.
 */
const val GITHUB_CACHE_DIRECTORY = "github-cache"

/**
 * The kernel's logs directory.
 */
const val LOGS_DIRECTORY = "logs"

fun getFullPathToGitHubCacheDirectory(): Path = Paths.get(
    System.getProperty("user.home"),
    BOWLER_DIRECTORY,
    BOWLERKERNEL_DIRECTORY,
    GIT_CACHE_DIRECTORY,
    GITHUB_CACHE_DIRECTORY
)
