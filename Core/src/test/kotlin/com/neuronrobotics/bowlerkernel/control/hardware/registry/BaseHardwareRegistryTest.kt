/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.registry

import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.resourceid.SimpleResourceId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseHardwareRegistryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `register unregistered device`() {
        val registerError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        assertTrue(registerError.isEmpty())
    }

    @Test
    fun `register device twice`() {
        val registerError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val secondRegisterError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        assertTrue(registerError.isEmpty())
        assertTrue(secondRegisterError.nonEmpty())
    }

    @Test
    fun `unregister registered device`() {
        val registerError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val unregisterError = registry.unregisterDevice(
            SimpleDeviceId(
                "A"
            )
        )
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device twice`() {
        val registerError = registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val unregisterError = registry.unregisterDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val secondUnregisterError = registry.unregisterDevice(
            SimpleDeviceId(
                "A"
            )
        )
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `register device resource`() {
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val registerError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(registerError.isEmpty())
    }

    @Test
    fun `register device resource twice`() {
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val registerError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        val secondRegisterError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(registerError.isEmpty())
        assertTrue(secondRegisterError.nonEmpty())
    }

    @Test
    fun `register device resource without registering device first`() {
        val registerError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(registerError.nonEmpty())
    }

    @Test
    fun `unregister device resource`() {
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val registerError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        val unregisterError =
            registry.unregisterDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device resource twice`() {
        registry.registerDevice(
            SimpleDeviceId(
                "A"
            )
        )
        val registerError =
            registry.registerDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        val unregisterError =
            registry.unregisterDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        val secondUnregisterError =
            registry.unregisterDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device resource without registering device resource first`() {
        val unregisterError =
            registry.unregisterDeviceResource(
                SimpleDeviceId("A"),
                SimpleResourceId(
                    "B"
                )
            )
        assertTrue(unregisterError.nonEmpty())
    }
}
