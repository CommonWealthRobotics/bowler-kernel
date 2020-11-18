package com.commonwealthrobotics.bowlerkernel.cli

import com.commonwealthrobotics.bowlerkernel.server.KernelServer
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Daemon : CliktCommand() {

    override fun run() {
    }

    companion object {
        fun create() = Daemon().subcommands(Start.create(), Stop.create(), GetPort.create())

        internal val kernelServer by lazy { KernelServer() }
    }
}
