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

import com.commonwealthrobotics.bowlerkernel.di.BowlerKernelKoinContext
import org.junit.jupiter.api.AfterEach
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest

open class KoinTestFixture : KoinTest {

    private var additionalAfterEach: () -> Unit = {}

    @AfterEach
    fun afterEach() {
        additionalAfterEach()
        stopKoin()
    }

    fun additionalAfterEach(configure: () -> Unit) {
        additionalAfterEach = configure
    }

    companion object {

        fun testKoin(vararg module: Module) {
            BowlerKernelKoinContext.koinApp = koinApplication {
                modules(module.toList())
            }
        }
    }
}
