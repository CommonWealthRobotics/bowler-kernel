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
package com.neuronrobotics.bowlerkernel.scripting.factory

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.hardware.Script

interface GistScriptFactory {

    /**
     * Creates a [Script] from a gist.
     *
     * @param gistId The gist id.
     * @param filename The file name in the gist.
     * @return A [Script] on success, a [String] on error.
     */
    fun createScriptFromGist(gistId: String, filename: String): Either<String, Script>
}
