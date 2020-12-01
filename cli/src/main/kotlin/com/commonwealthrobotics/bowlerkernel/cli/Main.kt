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
import com.commonwealthrobotics.bowlerkernel.kerneldiscovery.NameClient
import com.commonwealthrobotics.bowlerkernel.kerneldiscovery.NameServer
import com.commonwealthrobotics.bowlerkernel.server.KernelServer
import org.jline.builtins.Completers
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.net.InetAddress

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

                return commands.firstOrNull { it.name == primaryCommandStringWithoutPrefix }?.helpMessage()
                    ?: getCommandNotFound(primaryCommandStringWithoutPrefix, commands)
            }

            val args = substrings.subList(1, substrings.size).map { it.trim() }.filter { it.isNotEmpty() }
            return commands.firstOrNull {
                it.name == primaryCommandString
            }?.invoke(args) ?: getCommandNotFound(primaryCommandString, commands)
        }

        private fun buildCompleter(commands: List<Command>) = Completers.TreeCompleter(
            commands.map(Command::node)
        )

        private fun getCommandNotFound(primaryCommand: String, commands: List<Command>) = """
            The command $primaryCommand is not recognized. Available commands are:
            ${commands.joinToString { it.name }}
        """.trimIndent()

        @JvmStatic
        fun main(args: Array<String>) {
            val kernelServer = KernelServer()
            var nameServer: NameServer? = null

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
                            help = "Gets the port number the kernel server is listening on."
                        ) {
                            val port = kernelServer.port
                            if (port == -1) {
                                "The kernel server is not running."
                            } else {
                                "$port"
                            }
                        }
                    )
                ),
                Command(
                    name = "discovery",
                    help = "Controls kernel name discovery.",
                    children = listOf(
                        Command(
                            name = "scan",
                            help = "Scans the network for all running kernel name servers.",
                            options = listOf(
                                option<InetAddress>(
                                    short = "m",
                                    long = "multicast-group",
                                    help = "The multicast group address to send packets to."
                                ),
                                option<Int>(
                                    short = "p",
                                    long = "port",
                                    help = "The destination port number. Int in the range 1..65535.",
                                    validator = { it in 1..65535 },
                                ),
                                option<Int>(
                                    short = "t",
                                    long = "timeout",
                                    help = "The UDP socket's receive timeout in milliseconds.",
                                    validator = { it > 0 }
                                )
                            )
                        ) {
                            NameClient.scan(
                                multicastGroup = it.option("address", NameServer.defaultMulticastGroup),
                                port = it.option("port", NameServer.defaultPort),
                                timeoutMs = it.option("timeout", 1000)
                            ).joinToString("\n") {
                                "${it.a} <- ${it.b}"
                            }
                        },
                        Command(
                            name = "start-server",
                            help = "Starts a name server for kernel discovery.",
                            options = listOf(
                                option<String>(
                                    short = "n",
                                    long = "name",
                                    help = "The name this kernel will respond to scans with. " +
                                        "A non-empty String with no whitespace.",
                                    required = true,
                                    validator = { it.isNotEmpty() && !it.contains(Regex("\\s")) }
                                ),
                                option<InetAddress>(
                                    short = "m",
                                    long = "multicast-group",
                                    help = "The multicast group to join."
                                ),
                                option<Int>(
                                    short = "p",
                                    long = "port",
                                    help = "The desired port number. Int in the range 1..65535.",
                                    validator = { it in 1..65535 },
                                ),
                            )
                        ) {
                            if (nameServer == null) {
                                val localNS = NameServer(
                                    desiredName = it.option("name"),
                                    multicastGroup = it.option("multicast-group", NameServer.defaultMulticastGroup),
                                    desiredPort = it.option("port", NameServer.defaultPort),
                                )
                                localNS.ensureStarted()
                                while (!localNS.isRunning.get()) { Thread.sleep(10) }

                                nameServer = localNS
                                "Responding with ${localNS.name} on ${localNS.address}:${localNS.port}"
                            } else {
                                "A name server is already running."
                            }
                        },
                        Command(
                            name = "stop-server",
                            help = "Stops the currently running name server."
                        ) {
                            nameServer?.let {
                                it.ensureStopped()
                                while (it.isRunning.get()) { Thread.sleep(10) }
                                nameServer = null
                                "Stopped the name server."
                            } ?: "A name server was not running."
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

                terminal.writer().println(dispatch(line, commands).trimIndent())
                terminal.writer().println()
            }
        }
    }
}

/**
 * Gets the value of a required option.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> List<Tuple2<Option, *>>.option(long: String): T = first { it.a.long == long }.b as T

/**
 * Gets the value of a non-required option or a default value if the option was not set.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> List<Tuple2<Option, *>>.option(long: String, default: T): T =
    firstOrNull { it.a.long == long }?.b as T? ?: default
