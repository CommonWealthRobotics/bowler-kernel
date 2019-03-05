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

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Singleton
import com.neuronrobotics.bowlerkernel.hardware.device.DeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResourceFactory
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.hardware.registry.HardwareRegistryTracker
import org.jlleitschuh.guice.key
import org.jlleitschuh.guice.module
import org.octogonapus.guavautil.collections.immutableListOf
import org.octogonapus.guavautil.collections.toImmutableList

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

    private val scriptModules = mutableListOf<Module>()

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
        scriptModules.addAll(modules)
        injector = injector.createChildInjector(modules)
    }

    /**
     * Adds additional modules to the [injector].
     *
     * @param modules The modules to add.
     */
    @SuppressWarnings("SpreadOperator")
    fun addToInjector(vararg modules: Module) = addToInjector(immutableListOf(*modules))

    /**
     * Returns the modules which have been added to this script's [injector].
     *
     * @return The modules added to this script's [injector].
     */
    fun getModules(): ImmutableList<Module> = scriptModules.toImmutableList()

    companion object {
        private fun scriptModule() = module {
            bind<HardwareRegistryTracker>().`in`(Singleton::class.java)
            bind<HardwareRegistry>().to<HardwareRegistryTracker>().`in`(Singleton::class.java)
        }
    }
}
