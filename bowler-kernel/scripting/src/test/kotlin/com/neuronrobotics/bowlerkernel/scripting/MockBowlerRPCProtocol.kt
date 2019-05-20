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
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import arrow.core.right
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.DigitalState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.nongroup.IMUState
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import org.junit.jupiter.api.fail

/**
 * A mock implementation of the [BowlerRPCProtocol] which just checks for simple management of
 * connection state.
 */
@SuppressWarnings("NotImplementedDeclaration")
class MockBowlerRPCProtocol : BowlerRPCProtocol {

    private var isConnected = false

    override fun connect(): Either<String, Unit> {
        isConnected = true
        return Unit.right()
    }

    override fun disconnect(): Either<String, Unit> {
        isConnected = false
        return Unit.right()
    }

    override fun addPollingRead(resourceId: ResourceId) = runMockDiscovery()

    override fun addPollingReadGroup(resourceIds: ImmutableSet<ResourceId>) = runMockDiscovery()

    override fun addRead(resourceId: ResourceId) = runMockDiscovery()

    override fun addReadGroup(resourceIds: ImmutableSet<ResourceId>) = runMockDiscovery()

    override fun addWrite(resourceId: ResourceId) = runMockDiscovery()

    override fun addWriteGroup(resourceIds: ImmutableSet<ResourceId>) = runMockDiscovery()

    override fun isResourceInRange(resourceId: ResourceId) = waitAndReturn { true }

    override fun readProtocolVersion() = waitAndReturn { "" }

    private fun runMockDiscovery() = waitAndReturn { Unit.right() }

    private fun <T> waitAndReturn(toReturn: () -> T): T {
        if (isConnected) {
            Thread.sleep(5)
            return toReturn()
        } else {
            fail { "The RPC is not connected!" }
        }
    }

    override fun analogRead(resourceId: ResourceId): Double {
        fail { "Not implemented" }
    }

    override fun analogRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Double> {
        fail { "Not implemented" }
    }

    override fun analogWrite(resourceId: ResourceId, value: Short) {
        fail { "Not implemented" }
    }

    override fun analogWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Short>>) {
        fail { "Not implemented" }
    }

    override fun buttonRead(resourceId: ResourceId): Boolean {
        fail { "Not implemented" }
    }

    override fun buttonRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Boolean> {
        fail { "Not implemented" }
    }

    override fun digitalRead(resourceId: ResourceId): DigitalState {
        fail { "Not implemented" }
    }

    override fun digitalRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<DigitalState> {
        fail { "Not implemented" }
    }

    override fun digitalWrite(resourceId: ResourceId, value: DigitalState) {
        fail { "Not implemented" }
    }

    override fun digitalWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, DigitalState>>) {
        fail { "Not implemented" }
    }

    override fun encoderRead(resourceId: ResourceId): Long {
        fail { "Not implemented" }
    }

    override fun encoderRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Long> {
        fail { "Not implemented" }
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int) {
        fail { "Not implemented" }
    }

    override fun toneWrite(resourceId: ResourceId, frequency: Int, duration: Long) {
        fail { "Not implemented" }
    }

    override fun serialWrite(resourceId: ResourceId, message: String) {
        fail { "Not implemented" }
    }

    override fun serialRead(resourceId: ResourceId): String {
        fail { "Not implemented" }
    }

    override fun servoWrite(resourceId: ResourceId, angle: Double) {
        fail { "Not implemented" }
    }

    override fun servoWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Double>>) {
        fail { "Not implemented" }
    }

    override fun stepperWrite(resourceId: ResourceId, steps: Int, speed: Int) {
        fail { "Not implemented" }
    }

    override fun stepperWrite(resourcesAndValues: ImmutableList<Pair<ResourceId, Pair<Int, Int>>>) {
        fail { "Not implemented" }
    }

    override fun ultrasonicRead(resourceId: ResourceId): Long {
        fail { "Not implemented" }
    }

    override fun ultrasonicRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<Long> {
        fail { "Not implemented" }
    }

    override fun imuRead(resourceId: ResourceId): IMUState {
        fail { "Not implemented" }
    }

    override fun imuRead(resourceIds: ImmutableList<ResourceId>): ImmutableList<IMUState> {
        fail { "Not implemented" }
    }
}
