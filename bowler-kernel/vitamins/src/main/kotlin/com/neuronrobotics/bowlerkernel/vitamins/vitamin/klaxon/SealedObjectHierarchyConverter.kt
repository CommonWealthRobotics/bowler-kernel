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
import kotlin.reflect.full.isSubclassOf

/**
 * Converter sealed hierarchies of sealed classes and objects.
 *
 * @param kls The top-level class forming the hierarchy.
 */
class SealedObjectHierarchyConverter(private val kls: KClass<*>) : Converter {

    init {
        require(isSealedAndContainsOnlySealedClassesOrObjects(kls))
    }

    private val allClasses by lazy { allSealedSubclasses(kls) }

    override fun canConvert(cls: Class<*>) = cls.kotlin.isSubclassOf(kls)

    override fun fromJson(jv: JsonValue): Any? {
        val objectName = jv.objString("name")
        return allClasses.firstOrNull { it.qualifiedName == objectName }?.objectInstance
    }

    override fun toJson(value: Any): String {
        return """{"name": "${value::class.qualifiedName}"}"""
    }

    private fun isSealedAndContainsOnlySealedClassesOrObjects(cls: KClass<*>): Boolean =
        (cls.isSealed && cls.sealedSubclasses.map {
            isSealedAndContainsOnlySealedClassesOrObjects(it)
        }.fold(true, Boolean::and)) || cls.objectInstance != null

    private fun allSealedSubclasses(cls: KClass<*>): List<KClass<*>> =
        cls.sealedSubclasses + cls.sealedSubclasses.flatMap { allSealedSubclasses(it) }
}
