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
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.toFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import javafx.scene.paint.Color
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet
import org.octogonapus.ktguava.collections.toImmutableSetMultimap
import kotlin.concurrent.thread

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
    private val lengthForParamZero: Double = 0.1
) : CadGenerator {

    private val updateCadThreads = mutableListOf<Thread>()

    override fun generateBody(base: KinematicBase): CSG =
        Cube(bodyThickness, bodyThickness, bodyThickness).toCSG()

    @SuppressWarnings("ComplexMethod", "SwallowedException")
    override fun generateLimbs(base: KinematicBase): ImmutableSetMultimap<LimbId, ImmutableSet<CSG>> {
        return base.limbs.map { limb ->
            val limbCad = limb.links.map { link ->
                val rLink = Cube(
                    if (link.dhParam.r == 0.0) lengthForParamZero else link.dhParam.r,
                    cuboidThickness,
                    cuboidThickness
                ).toCSG().toXMax().moveByDhParam(immutableListOf(link.dhParam), false).apply {
                    color = Color.RED
                }

                val dLink = Cube(
                    cuboidThickness,
                    cuboidThickness,
                    if (link.dhParam.d == 0.0) lengthForParamZero else link.dhParam.d
                ).toCSG().toZMin().apply {
                    color = Color.GREEN
                }

                immutableSetOf(rLink, dLink)
            }.toImmutableSet()

            updateCadThreads.add(
                thread(name = "Update Limb CAD (${limb.id})", isDaemon = true) {
                    val linkTransforms =
                        limb.links.map { it.dhParam.frameTransformation }.toImmutableList()
                    val limbAngleBuffer = MutableList(limb.jointAngleControllers.size) {
                        FrameTransformation.identity
                    }

                    while (!Thread.currentThread().isInterrupted) {
                        updateLimb(
                            limbCad,
                            linkTransforms,
                            limb.jointAngleControllers.map(limbAngleBuffer) {
                                FrameTransformation.fromRotation(0, 0, it.getCurrentAngle())
                            }
                        )

                        try {
                            Thread.sleep(20)
                        } catch (ex: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
            )

            limb.id to limbCad
        }.toImmutableSetMultimap()
    }

    /**
     * Stops the threads updating the [CSG.manipulator] affines.
     */
    fun stopThreads() = updateCadThreads.forEach { it.interrupt() }

    companion object {

        /**
         * Updates the CAD for a limb with new frameTransforms. Writes directly to the
         * [CSG.manipulator].
         *
         * @param cad The limb CAD.
         * @param linkTransforms A [FrameTransformation] for each link's [DhParam].
         * @param frameTransforms The new link frame transforms.
         */
        internal fun updateLimb(
            cad: ImmutableSet<ImmutableSet<CSG>>,
            linkTransforms: List<FrameTransformation>,
            frameTransforms: List<FrameTransformation>
        ) {
            val dhTransformList = mutableListOf<FrameTransformation>()
            var transform = FrameTransformation.identity
            for (i in 0 until linkTransforms.size) {
                val dhTransform = if (i == 0)
                    FrameTransformation.identity
                else
                    linkTransforms[i - 1]

                transform *= dhTransform * frameTransforms[i]
                dhTransformList.add(transform)
            }

            cad.forEachIndexed { index, cadSet ->
                cadSet.forEach {
                    dhTransformList[index].setAffine(it.manipulator)
                }
            }
        }

        /**
         * Moves [this] CSG by the [FrameTransformation] represented by the [dhParams].
         *
         * @param dhParams The params to move this CSG by.
         * @param inverse Whether to invert the [FrameTransformation].
         * @return The moved CSG.
         */
        internal fun CSG.moveByDhParam(
            dhParams: Collection<DhParam>,
            inverse: Boolean = false
        ): CSG =
            transformed(
                dhParams.toFrameTransformation().toTransform().apply { if (inverse) invert() }
            )

        /**
         * Returns a list containing the results of applying the given [transform] function
         * to each element in the original collection.
         *
         * @param target The list to write the mapped values to.
         * @param transform The mapping function.
         * @return The mapped list.
         */
        private inline fun <T, R> Iterable<T>.map(
            target: MutableList<R>,
            transform: (T) -> R
        ): List<R> {
            forEachIndexed { index, element ->
                target[index] = transform(element)
            }

            return target
        }
    }
}
