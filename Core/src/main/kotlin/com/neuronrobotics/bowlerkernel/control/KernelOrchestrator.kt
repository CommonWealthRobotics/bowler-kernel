package com.neuronrobotics.bowlerkernel.control

class KernelOrchestrator {

    fun startControlScript(controlScript: ControlScript) {
        controlScript.start()
    }

    fun stopControlScript(controlScript: ControlScript) {
        controlScript.stopAndCleanUp()
    }
}
