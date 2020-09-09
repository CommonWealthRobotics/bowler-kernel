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

/**
 * Encapsulates the ways in which the client can provide credentials. No credentials received via this interface should
 * ever be stored for longer than necessary. Always favor requesting credentials again over storing them. NEVER store
 * credentials in a file (hashed or not).
 */
interface CredentialsProvider {

    /**
     * Get the credentials for the [remote] (e.g. `https://github.com/CommonWealthRobotics/BowlerBuilder.git`). If the
     * [remote] ends with `.git`, it is assumed the credentials will be used for authenticating via `git`. Otherwise,
     * it is assumed the credentials are for a Git remote.
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
