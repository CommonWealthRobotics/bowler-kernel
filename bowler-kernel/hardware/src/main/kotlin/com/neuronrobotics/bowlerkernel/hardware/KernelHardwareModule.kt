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
package com.neuronrobotics.bowlerkernel.hardware

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.hardware.registry.BaseHardwareRegistry
import org.jlleitschuh.guice.module

internal object KernelHardwareModule {
    /**
     * This [Injector] is static because it maintains a global [BaseHardwareRegistry].
     */
    internal val injector: Injector = Guice.createInjector(kernelHardwareModule())

    private fun kernelHardwareModule() = module {
        bind<BaseHardwareRegistry>().`in`(Singleton::class.java)
    }
}
