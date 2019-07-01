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
package com.neuronrobotics.bowlerkernel.kinematics.motion.model

import arrow.core.left
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

interface IFoo
data class Foo(val bar: Int) : IFoo

internal class ControllerSpecificationTest {

    private val klaxon = Klaxon()
    private val fooSpec = ControllerSpecification.fromEither(
        ClassData(
            Foo::class.qualifiedName!!,
            """{"bar" : 2}"""
        ).right()
    )

    private val gitFileSpec = ControllerSpecification.fromEither(
        GitFile("", "").left()
    )

    @Test
    fun `test parsing as concrete class`() {
        val jsonString = klaxon.toJsonString(fooSpec)
        val result = klaxon.parse<ControllerSpecification>(jsonString)!!.loadClass<Foo>(klaxon)
        assertEquals(Foo(2), result)
    }

    @Test
    fun `test parsing as interface`() {
        val jsonString = klaxon.toJsonString(fooSpec)
        val result = klaxon.parse<ControllerSpecification>(jsonString)!!.loadClass<IFoo>(klaxon)
        assertEquals(Foo(2), result)
    }

    @Test
    fun `test parsing as wrong class`() {
        val jsonString = klaxon.toJsonString(fooSpec)

        assertThrows<IllegalArgumentException> {
            klaxon.parse<ControllerSpecification>(jsonString)!!.loadClass<String>(klaxon)
        }
    }

    @Test
    fun `test parsing git file`() {
        val jsonString = klaxon.toJsonString(gitFileSpec)
        val result = klaxon.parse<ControllerSpecification>(jsonString)?.gitFile
        assertEquals(gitFileSpec.gitFile, result)
    }
}
