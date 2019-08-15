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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class SealedObjectHierarchyConverterTest {

    @ParameterizedTest
    @MethodSource("canConvertVexWheelSource")
    fun `test canConvert vex wheel`(cls: Class<*>, expected: Boolean) {
        assertEquals(expected, SealedObjectHierarchyConverter(cls.kotlin).canConvert(cls))
    }

    @ParameterizedTest
    @MethodSource("canConvertFooSource")
    fun `test canConvert foo`(cls: Class<*>, expected: Boolean) {
        assertEquals(expected, SealedObjectHierarchyConverter(cls.kotlin).canConvert(cls))
    }

    @Test
    fun `test canConvert broken foo`() {
        assertThrows<IllegalArgumentException> {
            SealedObjectHierarchyConverter(BrokenFoo::class)
        }
    }

    @ParameterizedTest
    @MethodSource("allVexWheelClasses")
    fun `test toJson`(cls: KClass<*>) {
        val expected = """
            {"name": "${cls.qualifiedName}"}
        """.trimIndent().trimStart().trimEnd()
        assertEquals(expected, SealedObjectHierarchyConverter(cls).toJson(cls.objectInstance!!))
    }

    @ParameterizedTest
    @MethodSource("allVexWheelClasses")
    fun `test fromJson`(cls: KClass<*>) {
        val converter = SealedObjectHierarchyConverter(DefaultVexWheel::class)
        val expected = cls.objectInstance!!
        val result = Klaxon()
            .converter(converter)
            .parse<DefaultVexWheel>(converter.toJson(expected))

        assertEquals(expected, result)
    }

    companion object {

        sealed class Foo {
            sealed class Bar : Foo() {
                object BarA : Bar()
                object BarB : Bar()
            }

            sealed class Empty : Foo()

            object FooA : Foo()
        }

        @Suppress("unused")
        sealed class BrokenFoo {

            sealed class Empty : BrokenFoo()

            object FooA : BrokenFoo()

            @Suppress("CanSealedSubClassBeObject")
            class Bar : BrokenFoo()
        }

        @JvmStatic
        fun allVexWheelClasses() = listOf(
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
        fun canConvertVexWheelSource() = allVexWheelClasses().map {
            Arguments.of(it.java, true)
        }

        @Suppress("unused")
        @JvmStatic
        fun canConvertFooSource() = listOf(
            Arguments.of(Foo::class.java, true),
            Arguments.of(Foo.Bar::class.java, true),
            Arguments.of(Foo.Bar.BarA::class.java, true),
            Arguments.of(Foo.Bar.BarB::class.java, true),
            Arguments.of(Foo.Empty::class.java, true),
            Arguments.of(Foo.FooA::class.java, true)
        )
    }
}
