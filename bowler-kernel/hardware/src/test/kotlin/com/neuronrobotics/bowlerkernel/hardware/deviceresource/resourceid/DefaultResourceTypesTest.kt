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
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThanOrEqualTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.lessThanOrEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

@Timeout(value = 30, unit = TimeUnit.SECONDS)
internal class DefaultResourceTypesTest {

    @ParameterizedTest
    @MethodSource("resourceTypeBoundsSource")
    fun `test resource type is within the type bounds`(resourceType: DefaultResourceTypes) {
        assertAll(
            {
                assertThat(
                    resourceType.type,
                    greaterThanOrEqualTo(DefaultResourceTypes.getLowestTypeNumber())
                )
            },
            {
                assertThat(
                    resourceType.type,
                    lessThanOrEqualTo(DefaultResourceTypes.getHighestTypeNumber())
                )
            }
        )
    }

    @Test
    fun `test resource types have unique ids`() {
        val types = resourceTypeBoundsSource().map { it.type }
        assertThat(types.toSet(), hasSize(equalTo(types.size)))
    }

    companion object {

        @Suppress("UNUSED")
        @JvmStatic
        fun resourceTypeBoundsSource(): List<DefaultResourceTypes> =
            DefaultResourceTypes::class.nestedClasses
                .filter { !it.isCompanion }.map { it.objectInstance as DefaultResourceTypes }
    }
}
