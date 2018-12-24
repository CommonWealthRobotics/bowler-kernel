/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.closedloop.util

class Timer {

    private val firstCalled = System.currentTimeMillis()
    private var lastCalled = firstCalled
    private var mark = firstCalled
    private var hardMark = 0L

    /**
     * Returns the time passed (ms) since the previous call of this function.
     *
     * @return The time passed (ms) since the previous call of this function.
     */
    fun getDt(): Long {
        val currTime = System.currentTimeMillis()
        val dt = currTime - lastCalled
        lastCalled = currTime
        return dt
    }

    /**
     * Returns the time passed (ms) since the previous call of [getDt]. Does not change the time
     * recorded by [getDt].
     *
     * @return The time passed (ms) since the previous call of [getDt].
     */
    fun readDt(): Long = System.currentTimeMillis() - lastCalled

    /**
     * Returns the time this timer was first constructed.
     *
     * @return The time this timer was first constructed.
     */
    fun getStartingTime(): Long = firstCalled

    /**
     * Returns the time since this timer was first constructed.
     *
     * @return The time since this timer was first constructed.
     */
    fun getDtFromStart(): Long = System.currentTimeMillis() - firstCalled

    /**
     * Places a time marker. Placing another marker will overwrite the previous one.
     */
    fun placeMark() {
        mark = System.currentTimeMillis()
    }

    /**
     * Clears the mark.
     *
     * @return The old mark.
     */
    fun clearMark(): Long {
        val oldMark = mark
        mark = 0
        return oldMark
    }

    /**
     * Places a hard time marker. Placing another marker will not overwrite the previous one. To
     * change a hard mark, first call [clearHardMark].
     */
    fun placeHardMark() {
        if (hardMark == 0L) {
            hardMark = System.currentTimeMillis()
        }
    }

    /**
     * Clears the hard mark.
     *
     * @return The old hard mark.
     */
    fun clearHardMark(): Long {
        val oldMark = hardMark
        hardMark = 0
        return oldMark
    }

    /**
     * Returns the time (ms) since the mark. Returns 0 if there is no mark.
     *
     * @return The time (ms) since the mark, 0 if there is no mark.
     */
    fun getDtFromMark(): Long {
        return if (mark == 0L) {
            0
        } else {
            System.currentTimeMillis() - mark
        }
    }

    /**
     * Returns the time (ms) since the hard mark. Returns 0 if there is no hard mark.
     *
     * @return The time (ms) since the hard mark, 0 if there is no hard mark.
     */
    fun getDtFromHarkMark(): Long {
        return if (hardMark == 0L) {
            0
        } else {
            System.currentTimeMillis() - hardMark
        }
    }
}
