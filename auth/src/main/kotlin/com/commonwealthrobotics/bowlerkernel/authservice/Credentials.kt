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
 * Encapsulates the different ways authentication can happen.
 */
sealed class Credentials {

    /**
     * "Traditional" credentials.
     *
     * @param username The username.
     * @param password Either a password or a personal access token.
     */
    data class Basic(val username: String, val password: String) : Credentials()

    /**
     * OAuth authentication.
     *
     * @param token The OAuth token.
     */
    data class OAuth(val token: String) : Credentials()

    /**
     * Anonymous authentication.
     */
    object Anonymous : Credentials()

    /**
     * Authentication is not allowed.
     */
    object Denied : Credentials()
}
