/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
@file:Suppress("TooManyFunctions", "UndocumentedPublicFunction")

package com.neuronrobotics.kinematicschef.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Multimap

fun <T> Iterable<T>.toImmutableList(): ImmutableList<T> = ImmutableList.copyOf(this)

fun ByteArray.toImmutableList(): ImmutableList<Byte> = toList().toImmutableList()

fun CharArray.toImmutableList(): ImmutableList<Char> = toList().toImmutableList()

fun ShortArray.toImmutableList(): ImmutableList<Short> = toList().toImmutableList()

fun IntArray.toImmutableList(): ImmutableList<Int> = toList().toImmutableList()

fun LongArray.toImmutableList(): ImmutableList<Long> = toList().toImmutableList()

fun FloatArray.toImmutableList(): ImmutableList<Float> = toList().toImmutableList()

fun DoubleArray.toImmutableList(): ImmutableList<Double> = toList().toImmutableList()

fun BooleanArray.toImmutableList(): ImmutableList<Boolean> = toList().toImmutableList()

fun <T> Array<out T>.toImmutableList(): ImmutableList<T> = toList().toImmutableList()

fun <T> immutableListOf(vararg elements: T): ImmutableList<T> =
    ImmutableList.copyOf(elements.toList())

fun <T> emptyImmutableList(): ImmutableList<T> = ImmutableList.of()

fun <T> Iterable<T>.toImmutableSet(): ImmutableSet<T> = ImmutableSet.copyOf(this)

fun ByteArray.toImmutableSet(): ImmutableSet<Byte> = toSet().toImmutableSet()

fun CharArray.toImmutableSet(): ImmutableSet<Char> = toSet().toImmutableSet()

fun ShortArray.toImmutableSet(): ImmutableSet<Short> = toSet().toImmutableSet()

fun IntArray.toImmutableSet(): ImmutableSet<Int> = toSet().toImmutableSet()

fun LongArray.toImmutableSet(): ImmutableSet<Long> = toSet().toImmutableSet()

fun FloatArray.toImmutableSet(): ImmutableSet<Float> = toSet().toImmutableSet()

fun DoubleArray.toImmutableSet(): ImmutableSet<Double> = toSet().toImmutableSet()

fun BooleanArray.toImmutableSet(): ImmutableSet<Boolean> = toSet().toImmutableSet()

fun <T> Array<out T>.toImmutableSet(): ImmutableSet<T> = toSet().toImmutableSet()

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
