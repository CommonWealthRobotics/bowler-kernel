/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
@file:Suppress("TooManyFunctions", "UndocumentedPublicFunction")

package com.neuronrobotics.bowlerkernel.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
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

fun <K, V> Iterable<Pair<K, V>>.toImmutableMap(): ImmutableMap<K, V> =
    ImmutableMap.copyOf(toMap())

fun <K, V> immutableMapOf(vararg elements: Pair<K, V>): ImmutableMap<K, V> =
    ImmutableMap.copyOf(elements.toMap())

fun <K, V> emptyImmutableMap(): ImmutableMap<K, V> = ImmutableMap.of()

operator fun <T> ImmutableList<T>.plus(other: ImmutableList<T>): ImmutableList<T> =
    ImmutableList.builder<T>()
        .addAll(this)
        .addAll(other)
        .build()

operator fun <K, V> ImmutableListMultimap<K, V>.plus(other: ImmutableListMultimap<K, V>):
    ImmutableListMultimap<K, V> =
    ImmutableListMultimap.builder<K, V>()
        .putAll(this)
        .putAll(other)
        .build()

operator fun <K, V> ImmutableSetMultimap<K, V>.plus(other: ImmutableSetMultimap<K, V>):
    ImmutableSetMultimap<K, V> =
    ImmutableSetMultimap.builder<K, V>()
        .putAll(this)
        .putAll(other)
        .build()
