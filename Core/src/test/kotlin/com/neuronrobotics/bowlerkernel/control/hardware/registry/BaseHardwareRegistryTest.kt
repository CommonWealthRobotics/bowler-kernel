package com.neuronrobotics.bowlerkernel.control.hardware.registry

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseHardwareRegistryTest {

    private val registry = BaseHardwareRegistry()

    @Test
    fun `register unregistered device`() {
        val registerError = registry.registerDevice("A")
        assertTrue(registerError.isEmpty())
    }

    @Test
    fun `register device twice`() {
        val registerError = registry.registerDevice("A")
        val secondRegisterError = registry.registerDevice("A")
        assertTrue(registerError.isEmpty())
        assertTrue(secondRegisterError.nonEmpty())
    }

    @Test
    fun `unregister registered device`() {
        val registerError = registry.registerDevice("A")
        val unregisterError = registry.unregisterDevice("A")
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device twice`() {
        val registerError = registry.registerDevice("A")
        val unregisterError = registry.unregisterDevice("A")
        val secondUnregisterError = registry.unregisterDevice("A")
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `register device resource`() {
        registry.registerDevice("A")
        val registerError = registry.registerDeviceResource("A", "B")
        assertTrue(registerError.isEmpty())
    }

    @Test
    fun `register device resource twice`() {
        registry.registerDevice("A")
        val registerError = registry.registerDeviceResource("A", "B")
        val secondRegisterError = registry.registerDeviceResource("A", "B")
        assertTrue(registerError.isEmpty())
        assertTrue(secondRegisterError.nonEmpty())
    }

    @Test
    fun `register device resource without registering device first`() {
        val registerError = registry.registerDeviceResource("A", "B")
        assertTrue(registerError.nonEmpty())
    }

    @Test
    fun `unregister device resource`() {
        registry.registerDevice("A")
        val registerError = registry.registerDeviceResource("A", "B")
        val unregisterError = registry.unregisterDeviceResource("A", "B")
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
    }

    @Test
    fun `unregister device resource twice`() {
        registry.registerDevice("A")
        val registerError = registry.registerDeviceResource("A", "B")
        val unregisterError = registry.unregisterDeviceResource("A", "B")
        val secondUnregisterError = registry.unregisterDeviceResource("A", "B")
        assertTrue(registerError.isEmpty())
        assertTrue(unregisterError.isEmpty())
        assertTrue(secondUnregisterError.nonEmpty())
    }

    @Test
    fun `unregister device resource without registering device resource first`() {
        val unregisterError = registry.unregisterDeviceResource("A", "B")
        assertTrue(unregisterError.nonEmpty())
    }
}
