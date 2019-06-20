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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin.klaxon

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import kotlin.reflect.KClass

/**
 * Converter for objects in nested, sealed hierarchies.
 *
 * @param cls The top-level class forming the hierarchy.
 */
class NestedObjectConverter(cls: KClass<*>) : Converter {

    private val allClasses by lazy {
        allNestedSubclasses(cls) + cls
    }

    override fun canConvert(cls: Class<*>) = allClasses.map { it.java }.contains(cls)

    override fun fromJson(jv: JsonValue): Any? {
        val objectName = jv.objString("name")
        return allClasses.firstOrNull { it.qualifiedName == objectName }?.objectInstance
    }

    override fun toJson(value: Any): String {
        require(value::class in allClasses)
        return """{"name": "${value::class.qualifiedName}"}"""
    }
}
