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

import org.jline.builtins.Completers

/**
 * @param name The name of the command that the user types to run the command.
 * @param help A help message explaining what the command does.
 * @param children Any sub-commands that will control the behavior of this command.
 * @param lambda If there are sub-commands, leave this empty. Otherwise, this lambda is called when the command runs.
 * Its output is printed to the console.
 */
internal data class Command(
    val name: String,
    private val help: String = "",
    private val children: List<Command> = emptyList(),
    private val lambda: (List<String>) -> String = if (children.isEmpty()) { _ -> "" }
    else { args ->
        Main.dispatch(args.joinToString(" "), children)
    },
) {

    internal operator fun invoke(args: List<String>) = lambda(args).prependIndent("  ")

    internal fun node(): Completers.TreeCompleter.Node = Completers.TreeCompleter.node(
        name,
        *children.map(Command::node).toTypedArray()
    )

    internal fun helpMessage(): String = if (children.isEmpty()) {
        help
    } else {
        """
        |$help
        |Available sub-commands:
        |${children.joinToString("\n") { "${it.name}: ${it.helpMessage()}" }.prependIndent(
            "  "
        )}
        """.trimMargin()
    }
}
