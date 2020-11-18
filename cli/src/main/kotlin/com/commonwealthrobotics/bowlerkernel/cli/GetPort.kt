package com.commonwealthrobotics.bowlerkernel.cli

import com.github.ajalt.clikt.core.CliktCommand

class GetPort : CliktCommand() {

    override fun run() {
        print(Daemon.kernelServer.port)
    }

    companion object {
        fun create() = GetPort()
    }
}
