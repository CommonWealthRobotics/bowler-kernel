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

import arrow.core.Invalid
import org.jline.builtins.Completers

internal class Command private constructor(
    val name: String,
    private val help: String,
    private val lambda1: (List<String>) -> String,
    private val lambda2: (List<Pair<Option, *>>) -> String,
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
    constructor(name: String, children: List<Command>, help: String) :
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
        help: String,
        options: List<Option> = emptyList(),
        lambda: (List<Pair<Option, *>>) -> String
    ) : this(
        name = name,
        help = help,
        lambda1 = { error("") },
        lambda2 = lambda,
        type = Type.Terminal,
        children = emptyList(),
        options = options,
    )

    /**
     * Runs this command.
     *
     * @param args The arguments to this command (not including this command itself).
     * @return The output of the command, to be echoed to the terminal.
     */
    operator fun invoke(args: List<String>): String {
        return if (type == Type.Terminal) {
            val matchedArgs = ArrayList(args)
            val matchedOptions = options.map {
                Pair(it, it.matchAndRemove(matchedArgs))
            }

            val matchedButNotParsedOptions = matchedOptions.filter { it.second is MatchResult.Invalid }
            if (matchedButNotParsedOptions.isNotEmpty()) {
                // These matched but resulted in parse errors
                return invalidOptions(matchedButNotParsedOptions)
            }

            val requiredButNotMatchedOptions =
                matchedOptions.filter { it.first.required && it.second is MatchResult.Unknown }
            if (requiredButNotMatchedOptions.isNotEmpty()) {
                // Not all required options matched
                return missingOptions(requiredButNotMatchedOptions.map { it.first })
            }

            if (matchedArgs.isNotEmpty()) {
                // There are unmatched options
                return unknownOptions(matchedArgs)
            }

            val invalidMatches =
                matchedOptions.filter { it.second is MatchResult.Match }
                    .filter { !it.first.validator((it.second as MatchResult.Match).data!!) }
            if (invalidMatches.isNotEmpty()) {
                // Some options have invalid values
                return invalidOptions(invalidMatches)
            }

            // All required options matched
            lambda2(
                matchedOptions.map {
                    val data = when (val match = it.second) {
                        is MatchResult.Match -> match.data
                        is MatchResult.Unknown -> null
                        else -> error("Unhandled match branch")
                    }
                    Pair(it.first, data)
                }
            ).prependIndent("  ")
        } else {
            lambda1(args).prependIndent("  ")
        }
    }

    private fun invalidOptions(invalidMatches: List<Pair<Option, Any?>>): String {
        val optionString = invalidMatches.joinToString("\n") { it.first.helpMessage }.prependIndent("  ")
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

    // node() requires varargs
    @SuppressWarnings("SpreadOperator")
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
