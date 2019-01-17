/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

interface PiezoelectricSpeaker : ProvisionedDeviceResource {

    /**
     * Plays a tone of [frequency] Hz.
     *
     * @param frequency The frequency of the tone in Hz.
     */
    fun playTone(frequency: Long)

    /**
     * Plays a tone of [frequency] Hz for [duration] ms.
     *
     * @param frequency The frequency of the tone in Hz.
     * @param duration The duration of the tone in ms.
     */
    fun playTone(frequency: Long, duration: Long)
}
