package com.neuronrobotics.bowlerkernel.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Multimap

fun <E> Iterable<E>.toImmutableList() = ImmutableList.copyOf(this)

fun <E> Iterable<E>.toImmutableSet() = ImmutableSet.copyOf(this)

fun <K, V> Multimap<K, V>.toImmutableSetMultimap() = ImmutableSetMultimap.copyOf(this)

fun <K, V> Multimap<K, V>.toImmutableListMultimap() = ImmutableListMultimap.copyOf(this)

operator fun <E> ImmutableList<E>.plus(other: ImmutableList<E>): ImmutableList<E> =
    ImmutableList.builder<E>()
        .addAll(this)
        .addAll(other)
        .build()
