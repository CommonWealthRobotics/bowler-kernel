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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

interface IFoo
data class Foo(val bar: Int) : IFoo

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class ControllerSpecificationTest {

    private val klaxon = Klaxon()
    private val fooSpec = ClassData(
        Foo::class.qualifiedName!!,
        """{"bar" : 2}"""
    ).right()

    private val gitFileSpec = GitFile("", "").left()

    @Test
    fun `test parsing as concrete class`() {
        val decoded = controllerSpecificationEncoder().run { fooSpec.encode() }
            .decode(controllerSpecificationDecoder())
        assertAll(
            { assertEquals(fooSpec.right(), decoded) },
            { assertEquals(Foo(2), (decoded as Either.Right).b.loadClass<Foo>(klaxon)) }
        )
    }

    @Test
    fun `test parsing as interface`() {
        val decoded = controllerSpecificationEncoder().run { fooSpec.encode() }
            .decode(controllerSpecificationDecoder())
        assertAll(
            { assertEquals(fooSpec.right(), decoded) },
            { assertEquals(Foo(2), (decoded as Either.Right).b.loadClass<IFoo>(klaxon)) }
        )
    }

    @Test
    fun `test parsing as wrong class`() {
        val decoded = controllerSpecificationEncoder().run { fooSpec.encode() }
            .decode(controllerSpecificationDecoder())
        assertAll(
            { assertEquals(fooSpec.right(), decoded) },
            {
                assertThrows<IllegalArgumentException> {
                    (decoded as Either.Right).b.loadClass<String>(klaxon)
                }
            }
        )
    }

    @Test
    fun `test parsing git file`() {
        val decoded = controllerSpecificationEncoder().run { gitFileSpec.encode() }
            .decode(controllerSpecificationDecoder())
        assertEquals(gitFileSpec.right(), decoded)
    }
}
