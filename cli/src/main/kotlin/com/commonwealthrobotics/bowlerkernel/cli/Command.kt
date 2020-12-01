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

import arrow.core.Tuple2
import org.jline.builtins.Completers

internal class Command private constructor(
    val name: String,
    private val help: String,
    private val lambda1: (List<String>) -> String,
    private val lambda2: (List<Tuple2<Option, *>>) -> String,
    private val type: Type,
    private val children: List<Command>,
    private val options: List<Option>,
) {

    private enum class Type {
        NonTerminal, Terminal
    }

    /**
     * @param name The name of the command that the user types to run the command.
     * @param children Any sub-commands that will control the behavior of this command.
     * @param help A help message explaining what the command does.
     */
    constructor(name: String, children: List<Command>, help: String = "") :
        this(
            name = name,
            help = help,
            lambda1 = { args -> Main.dispatch(args.joinToString(" "), children) },
            lambda2 = { error("") },
            type = Type.NonTerminal,
            children = children,
            options = emptyList(),
        )

    /**
     * @param name The name of the command that the user types to run the command.
     * @param help A help message explaining what the command does.
     * @param options Any options this command uses.
     * @param lambda Otherwise, this lambda is called when the command runs. Its output is printed to the console.
     */
    constructor(
        name: String,
        help: String = "",
        options: List<Option> = emptyList(),
        lambda: (List<Tuple2<Option, *>>) -> String
    ) : this(
        name = name,
        help = help,
        lambda1 = { error("") },
        lambda2 = lambda,
        type = Type.Terminal,
        children = emptyList(),
        options = options,
    )

    operator fun invoke(args: List<String>): String {
        return if (type == Type.Terminal) {
            val matchedArgs = ArrayList(args)
            val matchedOptions = options.map { Tuple2(it, it.matchAndRemove(matchedArgs)) }

            val requiredButNotMatchedOptions = matchedOptions.filter { it.a.required && it.b == null }
            if (requiredButNotMatchedOptions.isNotEmpty()) {
                // Not all required options matched
                return missingOptions(requiredButNotMatchedOptions.map { it.a })
            }

            if (matchedArgs.isNotEmpty()) {
                // There are unmatched options
                return unknownOptions(matchedArgs)
            }

            val invalidMatches = matchedOptions.filter { it.b != null }.filter { !it.a.validator(it.b!!) }
            if (invalidMatches.isNotEmpty()) {
                // Some options have invalid values
                return invalidOptions(invalidMatches)
            }

            // All required options matched
            lambda2(matchedOptions).prependIndent("  ")
        } else {
            lambda1(args).prependIndent("  ")
        }
    }

    private fun invalidOptions(invalidMatches: List<Tuple2<Option, Any?>>): String {
        val optionString = invalidMatches.joinToString("\n") { it.a.helpMessage }.prependIndent("  ")
        return """
        |Invalid options:
        |$optionString
        """.trimMargin()
    }

    private fun missingOptions(options: List<Option>): String {
        val optionString = options.joinToString("\n") { it.helpMessage }.prependIndent("  ")
        return """
        |Missing options:
        |$optionString
        """.trimMargin()
    }

    private fun unknownOptions(args: List<String>): String {
        val argString = args.joinToString("\n").prependIndent("  ")
        return """
        |Unknown options:
        |$argString
        """.trimMargin()
    }

    @SuppressWarnings("SpreadOperator") // node() requires varargs
    fun node(): Completers.TreeCompleter.Node = Completers.TreeCompleter.node(
        name,
        *children.map(Command::node).toTypedArray()
    )

    fun helpMessage(): String = if (type == Type.Terminal) {
        if (options.isEmpty()) {
            help
        } else {
            val optionString = options.joinToString("\n") { it.helpMessage }.prependIndent("    ")
            """
            |$help
            |  Options:
            |$optionString
            """.trimMargin()
        }
    } else {
        val childrenHelp = children.joinToString("\n") { "\n${it.name}: ${it.helpMessage()}" }.prependIndent("  ")
        """
        |$help
        |Available sub-commands:
        |$childrenHelp
        """.trimMargin()
    }
}
