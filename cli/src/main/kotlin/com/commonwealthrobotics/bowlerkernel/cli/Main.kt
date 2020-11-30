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

import com.commonwealthrobotics.bowlerkernel.server.KernelServer
import org.jline.builtins.Completers
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder

class Main {
    companion object {

        internal fun dispatch(line: String, commands: List<Command>): String {
            val substrings = line.split(Regex("\\s"))
            val primaryCommandString = substrings.firstOrNull() ?: return ""
            if (primaryCommandString.isEmpty()) {
                return ""
            }

            if (primaryCommandString.startsWith('?')) {
                // Dispatch in help mode
                val primaryCommandStringWithoutPrefix = primaryCommandString.substring(1)
                if (primaryCommandStringWithoutPrefix.isEmpty()) {
                    return ""
                }

                return commands.firstOrNull { it.name == primaryCommandStringWithoutPrefix }?.help
                    ?: getCommandNotFound(primaryCommandStringWithoutPrefix, commands)
            }

            return commands.firstOrNull {
                it.name == primaryCommandString
            }?.invoke(substrings.subList(1, substrings.size)) ?: getCommandNotFound(primaryCommandString, commands)
        }

        private fun buildCompleter(commands: List<Command>) =
            Completers.TreeCompleter(commands.map(Command::node))

        private fun getCommandNotFound(primaryCommand: String, commands: List<Command>) = """
            The command $primaryCommand is not recognized. Available commands are:
            ${commands.joinToString { it.name }}
        """.trimIndent()

        @JvmStatic
        fun main(args: Array<String>) {
            val kernelServer = KernelServer()

            val commands = listOf(
                Command(name = "help", help = "Prints information about Bowler and where to find more resources.") {
                    """
                        Welcome to Bowler. The full manual and many great tutorials and learning resources are available at:
                        https://commonwealthrobotics.com/
                    """.trimIndent()
                },
                Command(
                    name = "server",
                    help = "Controls the kernel server.",
                    children = listOf(
                        Command(name = "start", help = "Starts the kernel server.") {
                            kernelServer.ensureStarted()
                            ""
                        },
                        Command(name = "stop", help = "Stops the kernel server.") {
                            kernelServer.ensureStopped()
                            ""
                        },
                        Command(
                            name = "port",
                            help = "Gets the port number the kernel server is listening on. " +
                                "Returns -1 if the server is not running."
                        ) {
                            val port = kernelServer.port
                            if (port == -1) {
                                "The kernel server is not running."
                            } else {
                                "$port"
                            }
                        }
                    )
                )
            )

            val terminal = TerminalBuilder.builder().build()
            val completer = buildCompleter(commands)
            val lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build()
            val prompt = "bowler> "

            while (true) {
                val line: String = try {
                    lineReader.readLine(prompt)
                } catch (ex: UserInterruptException) {
                    ""
                } catch (ex: EndOfFileException) {
                    return
                }

                terminal.writer().println(dispatch(line, commands))
            }
        }
    }
}
