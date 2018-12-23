/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import com.google.common.collect.ImmutableList

/**
 * A generic script.
 */
interface Script {

    /**
     * Runs the script on the current thread.
     *
     * @param args The arguments to the script.
     * @return The result of the script.
     */
    fun runScript(args: ImmutableList<Any?>): Any?

    /**
     * Forces the script to stop.
     */
    fun stopScript()
}
