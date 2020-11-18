package com.commonwealthrobotics.bowlerkernel.cli

import com.github.ajalt.clikt.core.CliktCommand

class Stop : CliktCommand() {

    override fun run() {
        // TODO: Don't explode when stopping a server that was never started
        Daemon.kernelServer.stop()
    }

    companion object {
        fun create() = Stop()
    }
}
