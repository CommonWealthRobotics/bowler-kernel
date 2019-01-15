/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
