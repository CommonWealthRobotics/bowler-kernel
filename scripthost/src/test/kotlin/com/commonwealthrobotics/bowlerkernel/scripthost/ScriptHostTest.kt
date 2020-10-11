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
package com.commonwealthrobotics.bowlerkernel.scripthost

import com.commonwealthrobotics.bowlerkernel.testutil.KoinTestFixture
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.dsl.module

internal class ScriptHostTest : KoinTestFixture() {

    @Test
    fun `starting multiple concurrent sessions is an error`() {
        initKoin(module { })
        val host = ScriptHost(CoroutineScope(Dispatchers.Default), testLocalKoin)
        host.session(flowOf())
        shouldThrow<IllegalStateException> {
            host.session(flowOf())
        }
    }

    @Test
    fun `starting one session after another sequentially is okay`() {
        initKoin(module { })
        val host = ScriptHost(CoroutineScope(Dispatchers.Default), testLocalKoin)
        val session1 = host.session(flowOf())
        runBlocking { session1.collect() }
        val session2 = host.session(flowOf())
        runBlocking { session2.collect() }
    }
}
