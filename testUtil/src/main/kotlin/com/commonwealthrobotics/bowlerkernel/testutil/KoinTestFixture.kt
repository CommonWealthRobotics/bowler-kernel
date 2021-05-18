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

import org.junit.jupiter.api.AfterEach
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinApplication

open class KoinTestFixture {

    private var additionalAfterEach: () -> Unit = {}
    lateinit var testLocalKoin: KoinComponent
        private set

    /**
     * Sets the [testLocalKoin] instance to a new instance using the given modules.
     *
     * @param module The module(s) to start the instance with.
     */
    fun initKoin(vararg module: Module) {
        testLocalKoin = object : KoinComponent {
            val koinApp = koinApplication {
                modules(module.toList())
            }

            override fun getKoin(): Koin = koinApp.koin
        }
    }

    @AfterEach
    fun afterEach() {
        additionalAfterEach()
        stopKoin()
    }

    /**
     * Sets an [AfterEach] thunk that runs before Koin is stopped in the default implementation ([afterEach]).
     */
    fun additionalAfterEach(configure: () -> Unit) {
        additionalAfterEach = configure
    }
}
