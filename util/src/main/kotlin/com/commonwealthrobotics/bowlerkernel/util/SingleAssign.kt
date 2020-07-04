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
package com.commonwealthrobotics.bowlerkernel.util

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : Any?> singleAssign(): ReadWriteProperty<Any?, T> = SingleAssignVar()

fun <T : Any?> nullableSingleAssign(): ReadWriteProperty<Any?, T?> = NullableSingleAssignVar()

private class SingleAssignVar<T : Any?> : ReadWriteProperty<Any?, T> {

    private var value: T? = null
    private val wasValueSet = AtomicBoolean(false)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value ?: throw IllegalStateException(
            "Property ${property.name} should be initialized before get."
        )

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (wasValueSet.compareAndSet(false, true)) {
            this.value = value
        } else {
            throw IllegalStateException("Property ${property.name} can only be set once.")
        }
    }
}

private class NullableSingleAssignVar<T : Any?> : ReadWriteProperty<Any?, T?> {

    private var value: T? = null
    private val wasValueSet = AtomicBoolean(false)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (wasValueSet.compareAndSet(false, true)) {
            this.value = value
        } else {
            throw IllegalStateException("Property ${property.name} can only be set once.")
        }
    }
}
