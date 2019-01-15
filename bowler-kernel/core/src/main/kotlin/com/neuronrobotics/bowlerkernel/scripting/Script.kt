/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.control.hardware.KernelHardwareModule
import com.neuronrobotics.bowlerkernel.control.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistryTracker
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module

/**
 * A script with managed hardware access.
 */
abstract class Script {

    /**
     * An [Injector] available for the script to use.
     */
    protected var injector: Injector = KernelHardwareModule.injector.createChildInjector(
        scriptModule(),
        DeviceFactory.deviceFactoryModule(),
        UnprovisionedDeviceResourceFactory.unprovisionedDeviceResourceFactoryModule()
    )

    /**
     * Runs the script on the current thread.
     *
     * @param args The arguments to the script.
     * @return The result of the script.
     */
    abstract fun runScript(args: ImmutableList<Any?>): Either<String, Any?>

    /**
     * Forces the script to stop. Do not call this directly. Call `stopAndCleanUp()`.
     */
    protected abstract fun stopScript()

    /**
     * Stops the script and unregisters/disconnects all hardware it registered. Always call this
     * after the script is done running.
     */
    fun stopAndCleanUp() {
        stopScript()
        injector.getInstance(key<HardwareRegistryTracker>()).unregisterAllHardware()
    }

    /**
     * Adds additional modules to the [injector].
     *
     * @param modules The modules to add.
     */
    fun addToInjector(modules: ImmutableList<Module>) {
        injector = injector.createChildInjector(modules)
    }

    /**
     * Adds additional modules to the [injector].
     *
     * @param modules The modules to add.
     */
    @SuppressWarnings("SpreadOperator")
    fun addToInjector(vararg modules: Module) {
        injector = injector.createChildInjector(*modules)
    }

    companion object {
        private fun scriptModule() = module {
            bind<HardwareRegistryTracker>().`in`(Singleton::class.java)
            bind<HardwareRegistry>().to<HardwareRegistryTracker>().`in`(Singleton::class.java)
        }
    }
}
