package com.neuronrobotics.bowlerkernel.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet

fun <E> Iterable<E>.toImmutableList() = ImmutableList.copyOf(this)

fun <E> Iterable<E>.toImmutableSet() = ImmutableSet.copyOf(this)

operator fun <E> ImmutableList<E>.plus(other: ImmutableList<E>): ImmutableList<E> =
    ImmutableList.builder<E>()
        .addAll(this)
        .addAll(other)
        .build()
