/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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

/**
 * The script that is responsible for controlling the robot.
 */
abstract class ControlScript {

    /**
     * An [Injector] available for the script to use.
     */
    protected val injector: Injector = KernelHardwareModule.injector.createChildInjector(
        controlScriptHardwareModule(),
        DeviceFactory.deviceFactoryModule(),
        UnprovisionedDeviceResourceFactory.unprovisionedDeviceResourceFactoryModule()
    )

    /**
     * Starts the script on the current thread and blocks while it is running.
     */
    protected abstract fun runScript()

    /**
     * Signals to the script that it should stop and blocks until it stops.
     */
    protected abstract fun stopScript()

    /**
     * Asynchronously starts the script.
     */
    internal fun start() {
        runScript()
    }

    /**
     * Synchronously stops the script and unregisters all hardware it registered.
     */
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
