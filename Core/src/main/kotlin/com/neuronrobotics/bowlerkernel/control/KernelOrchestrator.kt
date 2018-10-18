package com.neuronrobotics.bowlerkernel.control

/**
 * The high-level orchestrator which controls various large lifecycle components.
 */
class KernelOrchestrator {

    /**
     * Starts a [ControlScript].
     */
    fun startControlScript(controlScript: ControlScript) {
        controlScript.start()
    }

    /**
     * Stops a [ControlScript].
     */
    fun stopControlScript(controlScript: ControlScript) {
        controlScript.stopAndCleanUp()
    }
}
