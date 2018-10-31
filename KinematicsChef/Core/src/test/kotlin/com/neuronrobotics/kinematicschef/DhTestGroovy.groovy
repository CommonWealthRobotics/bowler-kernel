package com.neuronrobotics.kinematicschef

import com.neuronrobotics.bowlerstudio.creature.ICadGenerator
import com.neuronrobotics.bowlerstudio.physics.TransformFactory
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Transform

ICadGenerator defaultCadGen = (ICadGenerator) ScriptingEngine.gitScriptRun(
        "https://github.com/madhephaestus/carl-the-hexapod.git",
        "ThreeDPrintCad.groovy",
        null
)

return new ICadGenerator() {

    private CSG moveDHValues(CSG incoming, DHLink dh) {
        TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
        Transform move = TransformFactory.nrToCSG(step)
        return incoming.transformed(move)

    }

    private CSG moveDH(CSG csg, DHLink dh) {
        return csg.movez(dh.d).rotz(dh.theta).movex(dh.r).rotx(dh.alpha)
    }

    private double length(DHLink link) {
        return Math.max(link.d, link.r)
    }

    @Override
    ArrayList<CSG> generateCad(DHParameterKinematics dh, int linkIndex) {
        return new ArrayList<CSG>()
    }

    @Override
    ArrayList<CSG> generateBody(MobileBase b) {
        def out = new ArrayList<CSG>()

        def links = b.appendages[0].chain.links
        for (int i = 0; i < links.size(); i++) {
            def link = links[i]
            def cube = new Cube(10.0, 10.0, length(link)).toCSG()

//            for (int j = 0; j <= i; j++) {
//                cube = moveDHValues(cube, links[j])
//            }
            cube = moveDHValues(cube, link)

            out.add(cube)
        }

        return out
    }
}
