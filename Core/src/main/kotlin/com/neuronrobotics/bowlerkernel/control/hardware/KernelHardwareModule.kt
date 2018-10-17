package com.neuronrobotics.bowlerkernel.control.hardware

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.control.hardware.registry.StaticHardwareRegistry
import org.jlleitschuh.guice.module

class KernelHardwareModule {

    companion object {
        internal val injector: Injector = Guice.createInjector(kernelHardwareModule())

        private fun kernelHardwareModule() = module {
            bind<StaticHardwareRegistry>().`in`(Singleton::class.java)
        }
    }
}
