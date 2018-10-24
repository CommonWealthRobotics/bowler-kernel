package com.neuronrobotics.bowlerkernel.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Multimap

fun <T> Iterable<T>.toImmutableList(): ImmutableList<T> = ImmutableList.copyOf(this)

fun <T> immutableListOf(vararg elements: T): ImmutableList<T> =
    ImmutableList.copyOf(elements.toList())

fun <T> emptyImmutableList(): ImmutableList<T> = ImmutableList.of()

fun <T> Iterable<T>.toImmutableSet(): ImmutableSet<T> = ImmutableSet.copyOf(this)

fun <T> immutableSetOf(vararg elements: T): ImmutableSet<T> =
    ImmutableSet.copyOf(elements.toSet())

fun <T> emptyImmutableSet(): ImmutableSet<T> = ImmutableSet.of()

fun <K, V> Multimap<K, V>.toImmutableListMultimap(): ImmutableListMultimap<K, V> =
    ImmutableListMultimap.copyOf(this)

fun <K, V> immutableListMultimapOf(vararg pairs: Pair<K, V>): ImmutableListMultimap<K, V> {
    val builder = ImmutableListMultimap.builder<K, V>()

    pairs.forEach {
        builder.put(it.first, it.second)
    }

    return builder.build()
}

fun <K, V> emptyImmutableListMultimap(): ImmutableListMultimap<K, V> = ImmutableListMultimap.of()

fun <K, V> Multimap<K, V>.toImmutableSetMultimap(): ImmutableSetMultimap<K, V> =
    ImmutableSetMultimap.copyOf(this)

fun <K, V> immutableSetMultimapOf(vararg pairs: Pair<K, V>): ImmutableSetMultimap<K, V> {
    val builder = ImmutableSetMultimap.builder<K, V>()

    pairs.forEach {
        builder.put(it.first, it.second)
    }

    return builder.build()
}

fun <K, V> emptyImmutableSetMultimap(): ImmutableSetMultimap<K, V> = ImmutableSetMultimap.of()

operator fun <T> ImmutableList<T>.plus(other: ImmutableList<T>): ImmutableList<T> =
    ImmutableList.builder<T>()
        .addAll(this)
        .addAll(other)
        .build()
