package com.neuronrobotics.bowlerkernel.control.hardware.device

import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import org.jlleitschuh.guice.module
import javax.inject.Inject

class DeviceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry
) : BowlerDeviceFactory {

    private fun registerDevice(deviceId: DeviceId) = registry.registerDevice(deviceId)

    override fun makeBowlerDevice(deviceId: DeviceId) = BowlerDevice(deviceId)

    companion object {

        internal fun deviceFactoryModule() = module {
            bind<BowlerDeviceFactory>().to<DeviceFactory>()
        }
    }
}
