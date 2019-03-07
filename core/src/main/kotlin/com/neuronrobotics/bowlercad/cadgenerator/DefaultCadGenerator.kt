/*
 * This file is part of bowler-cad.
 *
 * bowler-cad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-cad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-cad.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlercad.cadgenerator

import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.LimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import javafx.scene.paint.Color
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableSetMultimap

/**
 * A simple [CadGenerator] that visualizes DH params as colored cuboids. The [DhParam.r] term is
 * red and the [DhParam.d] term is green.
 *
 * @param bodyThickness The thickness of the body cube.
 * @param cuboidThickness The thickness of the DH param cuboids.
 * @param lengthForParamZero The length to use for a DH param cuboid when its d or r is zero.
 */
class DefaultCadGenerator(
    private val bodyThickness: Double = 5.0,
    private val cuboidThickness: Double = 5.0,
    private val lengthForParamZero: Double = 0.0
) : CadGenerator {

    override fun generateBody(base: KinematicBase): CSG =
        Cube(bodyThickness, bodyThickness, bodyThickness).toCSG()

    override fun generateLimbs(base: KinematicBase): ImmutableSetMultimap<LimbId, ImmutableSet<CSG>> {
        return base.limbs.map { limb ->
            limb.id to limb.links.map { link ->
                val rLink = Cube(
                    if (link.dhParam.r == 0.0) lengthForParamZero else link.dhParam.r,
                    cuboidThickness,
                    cuboidThickness
                ).toCSG().toXMax().apply {
                    color = Color.RED
                }

                val dLink = Cube(
                    cuboidThickness,
                    cuboidThickness,
                    if (link.dhParam.d == 0.0) lengthForParamZero else link.dhParam.d
                ).toCSG().toZMin().moveByDhParam(link.dhParam).apply {
                    color = Color.GREEN
                }

                immutableSetOf(rLink, dLink)
            }
        }.toImmutableSetMultimap()
    }

    private fun CSG.moveByDhParam(dhParam: DhParam): CSG =
        transformed(dhParam.frameTransformation.toTransform())
}
