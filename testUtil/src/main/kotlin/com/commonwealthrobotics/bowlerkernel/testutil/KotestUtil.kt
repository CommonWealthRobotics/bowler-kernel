/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.commonwealthrobotics.bowlerkernel.testutil

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.neverNullMatcher
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.property.Exhaustive

// TODO: Remove these once Kotest 4.1 is released.
fun <T> Array<T>.shouldHaveInOrder(vararg ps: (T) -> Boolean) =
    asList().shouldHaveInOrder(ps.toList())

fun <T> List<T>.shouldHaveInOrder(vararg ps: (T) -> Boolean) =
    this.shouldHaveInOrder(ps.toList())

infix fun <T> Array<T>.shouldHaveInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldHaveInOrder(expected)

infix fun <T> List<T>.shouldHaveInOrder(expected: List<(T) -> Boolean>) =
    this should hasInOrder(expected)

infix fun <T> Array<T>.shouldNotHaveInOrder(expected: Array<(T) -> Boolean>) =
    asList().shouldNotHaveInOrder(expected.asList())

infix fun <T> Array<T>.shouldNotHaveInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldNotHaveInOrder(expected)

infix fun <T> List<T>.shouldNotHaveInOrder(expected: List<(T) -> Boolean>) =
    this shouldNot hasInOrder(expected)

fun <T> hasInOrder(vararg ps: (T) -> Boolean): Matcher<Collection<T>?> = hasInOrder(ps.asList())

fun <T> Array<T>.shouldHaveExactlyInOrder(vararg ps: (T) -> Boolean) =
    asList().shouldHaveExactlyInOrder(ps.toList())

fun <T> List<T>.shouldHaveExactlyInOrder(vararg ps: (T) -> Boolean) =
    this.shouldHaveExactlyInOrder(ps.toList())

infix fun <T> Array<T>.shouldHaveExactlyInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldHaveExactlyInOrder(expected)

infix fun <T> List<T>.shouldHaveExactlyInOrder(expected: List<(T) -> Boolean>) =
    this should hasExactlyInOrder(expected)

infix fun <T> Array<T>.shouldNotHaveExactlyInOrder(expected: Array<(T) -> Boolean>) =
    asList().shouldNotHaveExactlyInOrder(expected.asList())

infix fun <T> Array<T>.shouldNotHaveExactlyInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldNotHaveExactlyInOrder(expected)

infix fun <T> List<T>.shouldNotHaveExactlyInOrder(expected: List<(T) -> Boolean>) =
    this shouldNot hasExactlyInOrder(expected)

fun <T> hasExactlyInOrder(vararg ps: (T) -> Boolean): Matcher<Collection<T>?> =
    hasExactlyInOrder(ps.asList())

/**
 * Assert that a collection has a subsequence matching the sequence of predicates, possibly with
 * values in between.
 *
 * TODO: Remove this once Kotest 4.1 is released.
 */
fun <T> hasInOrder(predicates: List<(T) -> Boolean>): Matcher<Collection<T>?> =
    neverNullMatcher { actual ->
        require(predicates.isNotEmpty()) { "predicates must not be empty" }

        var subsequenceIndex = 0
        val actualIterator = actual.iterator()

        while (actualIterator.hasNext() && subsequenceIndex < predicates.size) {
            if (predicates[subsequenceIndex](actualIterator.next())) subsequenceIndex += 1
        }

        MatcherResult(
            subsequenceIndex == predicates.size,
            { "$actual did not match the predicates $predicates in order" },
            { "$actual should not match the predicates $predicates in order" }
        )
    }

/**
 * Assert that a collection has a subsequence matching the sequence of predicates with no values in
 * between.
 *
 * TODO: Remove this once Kotest 4.1 is released.
 */
fun <T> hasExactlyInOrder(predicates: List<(T) -> Boolean>): Matcher<Collection<T>?> =
    neverNullMatcher { actual ->
        require(predicates.isNotEmpty()) { "predicates must not be empty" }

        var subsequenceIndex = 0
        val actualIterator = actual.iterator()

        while (actualIterator.hasNext() && subsequenceIndex < predicates.size) {
            val predicate = predicates[subsequenceIndex]
            val next = actualIterator.next()
            if (predicate(next)) subsequenceIndex += 1
            else {
                println("$next did not match the $predicate.")
                return@neverNullMatcher MatcherResult(
                    false,
                    { "$actual did not match the predicates $predicates in order" },
                    { "$actual should not match the predicates $predicates in order" }
                )
            }
        }

        MatcherResult(
            subsequenceIndex == predicates.size,
            { "$actual did not match the predicates $predicates in order" },
            { "$actual should not match the predicates $predicates in order" }
        )
    }

// TODO: Remove this once Kotest 4.1 is released.
fun <A, B : A, C : A> Exhaustive<B>.merge(other: Exhaustive<C>): Exhaustive<A> =
    object : Exhaustive<A>() {
        override val values: List<A> = this@merge.values.zip(other.values)
            .flatMap { listOf(it.first, it.second) }
    }
