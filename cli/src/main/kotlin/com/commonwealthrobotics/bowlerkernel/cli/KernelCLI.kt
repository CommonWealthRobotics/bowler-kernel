package com.commonwealthrobotics.bowlerkernel.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class KernelCLI : CliktCommand() {

    override fun run() {
    }

    companion object {
        fun create() = KernelCLI().subcommands(Daemon.create())
    }
}
