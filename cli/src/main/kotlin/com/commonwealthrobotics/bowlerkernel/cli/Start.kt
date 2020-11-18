package com.commonwealthrobotics.bowlerkernel.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class Start : CliktCommand() {

    private val port by option(help="The port number to start the server on. Set 0 to choose random free port.").int().default(0)

    override fun run() {
        // TODO: This needs to detach the server process so it continues to run as a daemon process
        Daemon.kernelServer.start(port = port)
        echo("Started daemon on port ${Daemon.kernelServer.port}")
    }

    companion object {
        fun create() = Start()
    }
}
