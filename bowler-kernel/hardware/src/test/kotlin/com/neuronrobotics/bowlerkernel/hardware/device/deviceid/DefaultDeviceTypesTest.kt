package com.neuronrobotics.bowlerkernel.hardware.device.deviceid

import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultDeviceTypesTest {

    @ParameterizedTest
    @MethodSource("digitalOutPinsSource")
    fun `test esp32-wroom-32 digital out pins`(pinNumber: Byte) {
        val actual = DefaultDeviceTypes.Esp32wroom32.isResourceInRange(
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(pinNumber)
            )
        )

        assertTrue(actual)
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun digitalOutPinsSource() =
            listOf<Byte>(2, 5, 12, 13, 15, 4, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 32, 33)
    }
}
