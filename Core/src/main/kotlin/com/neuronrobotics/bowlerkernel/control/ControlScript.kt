package com.neuronrobotics.bowlerkernel.control

import com.google.inject.Injector
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.control.hardware.KernelHardwareModule
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistryTracker
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module

abstract class ControlScript {

    protected val injector: Injector = KernelHardwareModule.injector.createChildInjector(
            controlScriptHardwareModule(),
            DeviceFactory.deviceFactoryModule(),
            UnprovisionedDeviceResourceFactory.unprovisionedDeviceResourceFactoryModule()
    )

    protected abstract fun runScript()

    protected abstract fun stopScript()

    internal fun start() {
        runScript()
    }

    internal fun stopAndCleanUp() {
        stopScript()
        injector.getInstance(key<HardwareRegistryTracker>()).unregisterAllHardware()
    }

    companion object {
        private fun controlScriptHardwareModule() = module {
            bind<HardwareRegistry>().to<HardwareRegistryTracker>().`in`(Singleton::class.java)
        }
    }
}
