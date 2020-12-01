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
package com.commonwealthrobotics.bowlerkernel.cli

import java.net.InetAddress
import kotlin.reflect.KClass

data class Option(
    val clazz: KClass<*>,
    val short: String,
    val long: String,
    private val additionalHelpMessage: String,
    val required: Boolean,
    val validator: (Any) -> Boolean,
) {

    val helpMessage = "-$short, --$long\t\t$additionalHelpMessage"

    /**
     * Match this option in the [args] and remove it if it matched.
     *
     * @param args The args from the cmdline.
     * @return The parsed value of this option.
     */
    fun matchAndRemove(args: MutableList<String>): Any? {
        val argIndex = args.indexOfFirst { it == "-$short" || it == "--$long" }
        if (argIndex == -1) {
            return null
        }

        val argValue = try {
            args[argIndex + 1]
        } catch (ex: IndexOutOfBoundsException) {
            null
        } ?: return null

        val value = parse(argValue)
        if (value != null) {
            args.removeAt(argIndex) // Remove the option
            args.removeAt(argIndex) // Remove its value
        }
        return value
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parse(value: String): Any? {
        return when (clazz) {
            Byte::class -> value.toByte()
            Short::class -> value.toShort()
            Int::class -> value.toInt()
            Long::class -> value.toLong()
            Float::class -> value.toFloat()
            Double::class -> value.toDouble()

            String::class -> value

            InetAddress::class ->
                try {
                    InetAddress.getByAddress(value.split('.').map { it.toInt().toByte() }.toByteArray())
                } catch (ex: Exception) {
                    null
                }

            else -> null
        }
    }
}

/**
 * An option that modifies the behavior of a [Command].
 *
 * @param short The short switch.
 * @param long The long switch.
 * @param help A message explaining what the option does and its validation requirements.
 * @param required Whether this option is required to be set.
 * @param validator Any validation of the option's value.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> option(
    short: String = "",
    long: String = "",
    help: String = "",
    required: Boolean = false,
    noinline validator: (T) -> Boolean = { true }
) = Option(T::class, short, long, help, required, validator as (Any) -> Boolean)
