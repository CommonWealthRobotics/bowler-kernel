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

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.vitamins.vitamin.defaultvitamin.DefaultVexWheel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

internal class NestedObjectConverterTest {

    private val converter = NestedObjectConverter(DefaultVexWheel::class)

    @ParameterizedTest
    @MethodSource("canConvertSource")
    fun `test canConvert`(cls: Class<*>, expected: Boolean) {
        assertEquals(expected, converter.canConvert(cls))
    }

    @ParameterizedTest
    @MethodSource("allConvertibleClasses")
    fun `test toJson`(cls: KClass<*>) {
        val expected = """
            {"name": "${cls.qualifiedName}"}
        """.trimIndent().trimStart().trimEnd()
        assertEquals(expected, converter.toJson(cls.objectInstance!!))
    }

    @ParameterizedTest
    @MethodSource("allConvertibleClasses")
    fun `test fromJson`(cls: KClass<*>) {
        val expected = cls.objectInstance!!

        val result = Klaxon()
            .converter(converter)
            .parse<DefaultVexWheel>(converter.toJson(expected))

        assertEquals(expected, result)
    }

    companion object {

        @JvmStatic
        fun allConvertibleClasses() = listOf(
            DefaultVexWheel.OmniWheel.Omni275::class,
            DefaultVexWheel.OmniWheel.Omni325::class,
            DefaultVexWheel.OmniWheel.Omni4::class,
            DefaultVexWheel.TractionWheel.Wheel275::class,
            DefaultVexWheel.TractionWheel.Wheel325::class,
            DefaultVexWheel.TractionWheel.Wheel4::class,
            DefaultVexWheel.TractionWheel.Wheel5::class,
            DefaultVexWheel.HighTraction::class,
            DefaultVexWheel.Mecanum::class,
            DefaultVexWheel.WheelLeg::class
        )

        @Suppress("unused")
        @JvmStatic
        fun canConvertSource() = allConvertibleClasses().map {
            Arguments.of(it.java, true)
        } + Arguments.of(Int::class.java, false)
    }
}
