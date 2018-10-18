package com.neuronrobotics.bowlerkernel.control.hardware

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.control.hardware.registry.BaseHardwareRegistry
import org.jlleitschuh.guice.module

class KernelHardwareModule {

    companion object {
        /**
         * This [Injector] is static because it maintains a global [BaseHardwareRegistry].
         */
        internal val injector: Injector = Guice.createInjector(kernelHardwareModule())

        private fun kernelHardwareModule() = module {
            bind<BaseHardwareRegistry>().`in`(Singleton::class.java)
        }
    }
}
