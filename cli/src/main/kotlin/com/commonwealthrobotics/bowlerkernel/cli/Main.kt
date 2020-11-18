package com.commonwealthrobotics.bowlerkernel.cli

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = KernelCLI.create().main(args)
    }
}
