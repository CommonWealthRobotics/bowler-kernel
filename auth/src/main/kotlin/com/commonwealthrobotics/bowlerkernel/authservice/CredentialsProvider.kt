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
package com.commonwealthrobotics.bowlerkernel.authservice

interface CredentialsProvider {

    /**
     * Get the credentials for the [remote] (e.g. `https://github.com/CommonWealthRobotics/BowlerBuilder.git`).
     *
     * These credentials should never be stored.
     *
     * @param remote The remote to ask for.
     * @return The credentials for that remote.
     */
    suspend fun getCredentialsFor(remote: String): Credentials

    /**
     * Get a 2FA response for the [remote] (e.g. `https://github.com/CommonWealthRobotics/BowlerBuilder.git`).
     *
     * @param remote The remote to ask for.
     * @return A 2FA code.
     */
    suspend fun getTwoFactorFor(remote: String): String
}
