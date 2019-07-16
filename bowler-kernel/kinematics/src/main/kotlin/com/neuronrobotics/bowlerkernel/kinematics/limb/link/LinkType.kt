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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import arrow.core.Either
import arrow.core.Try
import arrow.core.flatMap
import helios.core.DecodingError
import helios.core.JsString
import helios.core.Json
import helios.core.ObjectDecodingError
import helios.core.StringDecodingError
import helios.typeclasses.Decoder
import helios.typeclasses.Encoder

/**
 * The type of a link.
 */
enum class LinkType {
    Rotary, Prismatic;

    companion object
}

fun LinkType.Companion.encoder() = Enum.Companion.encoder<LinkType>()
fun LinkType.Companion.decoder() = Enum.Companion.decoder<LinkType>()

fun <E : Enum<E>> Enum.Companion.encoder(): Encoder<Enum<E>> = object : Encoder<Enum<E>> {
    override fun Enum<E>.encode(): Json = JsString(name)
}

inline fun <reified E : Enum<E>> Enum.Companion.decoder(): Decoder<E> =
    object : Decoder<E> {

        override fun decode(value: Json): Either<DecodingError, E> =
            value.asJsString().toEither { StringDecodingError(value) }.flatMap {
                Try {
                    java.lang.Enum.valueOf(E::class.java, it.value.toString())
                }.toEither { ObjectDecodingError(value) }
            }
    }
