package com.neuronrobotics.kinematicschef.util

internal fun Int.modulus(rhs: Int) =
    ((this % rhs) + rhs) % rhs

internal fun Double.modulus(rhs: Int) =
    ((this % rhs) + rhs) % rhs

internal fun Int.modulus(rhs: Double) =
    ((this % rhs) + rhs) % rhs

internal fun Double.modulus(rhs: Double) =
    ((this % rhs) + rhs) % rhs
