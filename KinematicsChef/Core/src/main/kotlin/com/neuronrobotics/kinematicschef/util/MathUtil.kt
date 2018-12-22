/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.util

internal fun Int.modulus(rhs: Int) =
    ((this % rhs) + rhs) % rhs

internal fun Double.modulus(rhs: Int) =
    ((this % rhs) + rhs) % rhs

internal fun Int.modulus(rhs: Double) =
    ((this % rhs) + rhs) % rhs

internal fun Double.modulus(rhs: Double) =
    ((this % rhs) + rhs) % rhs
