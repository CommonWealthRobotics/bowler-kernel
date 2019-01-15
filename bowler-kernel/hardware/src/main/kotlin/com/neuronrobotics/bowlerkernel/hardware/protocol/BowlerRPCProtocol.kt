/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.protocol

/**
 * An RPC protocol that all Bowler devices implement.
 */
interface BowlerRPCProtocol {

    /**
     * Temporary write method.
     */
    fun write()

    /**
     * Temporary read method.
     */
    fun read()
}