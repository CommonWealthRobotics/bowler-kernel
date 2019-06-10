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
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.Link
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.toFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.util.Limits
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Extrude
import javafx.scene.paint.Color
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.immutableSetOf
import org.octogonapus.ktguava.collections.plus
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableSet
import org.octogonapus.ktguava.collections.toImmutableSetMultimap
import kotlin.concurrent.thread
import kotlin.math.absoluteValue

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
    private val lengthForParamZero: Double = 0.1,
    private val axisLength: Double = 30.0,
    private val fanRadius: Double = 15.0
) : CadGenerator {

    private val Limits.range: Double
        get() = maximum - minimum

    private val updateCadThreads = mutableListOf<Thread>()

    override fun generateBody(base: KinematicBase): CSG =
        Cube(bodyThickness, bodyThickness, bodyThickness).toCSG()

    @SuppressWarnings("ComplexMethod", "SwallowedException")
    override fun generateLimbs(base: KinematicBase): ImmutableSetMultimap<LimbId, ImmutableSet<CSG>> {
        return base.limbs.map { limb ->
            val limbCad = limb.links.map { getCadForLink(it) }.toImmutableSet()

            updateCadThreads.add(
                thread(name = "Update Limb CAD (${limb.id})", isDaemon = true) {
                    val linkTransforms =
                        limb.links.map { it.dhParam.frameTransformation }.toImmutableList()

                    val limbAngleBuffer = MutableList(limb.jointAngleControllers.size) {
                        FrameTransformation.identity
                    }

                    val baseTransform = base.limbBaseTransforms[limb.id]!!

                    while (!Thread.currentThread().isInterrupted) {
                        updateLimb(
                            limbCad,
                            baseTransform,
                            linkTransforms,
                            limb.jointAngleControllers.map(limbAngleBuffer) {
                                FrameTransformation.fromRotation(it.getCurrentAngle(), 0, 0)
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
     * Generates the CAD for a link.
     *
     * @param link The link.
     * @return All CAD for the link.
     */
    private fun getCadForLink(link: Link): ImmutableSet<CSG> {
        return getAxes(link) +
            getThetaViz(link) +
            getAlphaViz(link) +
            getLimitsViz(link) +
            immutableSetOf(
                getDViz(link),
                getRViz(link)
            )
    }

    private fun getAxes(link: Link): ImmutableSet<CSG> {
        val xAxis = Cube(axisLength, 1.0, 1.0).toCSG().toXMin().apply { color = Color.RED }
        val yAxis = Cube(1.0, axisLength, 1.0).toCSG().toYMin().apply { color = Color.GREEN }
        val zAxis = Cube(1.0, 1.0, axisLength).toCSG().toZMin().apply { color = Color.BLUE }
        return immutableSetOf(
            xAxis.moveByDhParam(immutableListOf(link.dhParam), false),
            yAxis.moveByDhParam(immutableListOf(link.dhParam), false),
            zAxis.moveByDhParam(immutableListOf(link.dhParam), false)
        )
    }

    /**
     * Shows the value of [DhParam.d].
     *
     * @param link The link.
     * @return A CSG showing theta.
     */
    private fun getDViz(link: Link): CSG =
        Cube(
            cuboidThickness,
            cuboidThickness,
            if (link.dhParam.d == 0.0) lengthForParamZero else link.dhParam.d
        ).toCSG()
            .toZMin()
            .apply { color = Color.GREEN }

    /**
     * Shows the value of [DhParam.theta]. theta=0 is a straight line, theta=90 is a quarter
     * circle, etc.
     *
     * @param link The link.
     * @return A CSG showing theta.
     */
    private fun getThetaViz(link: Link): ImmutableSet<CSG> {
        val thetaProfile = Cube(fanRadius, 1.0, 1.0).toCSG()
            .toXMin()
            .toZMin()

        val start = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .toZMin()
            .apply { color = Color.WHITE }

        val end = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .toZMin()
            .rotz(link.dhParam.theta)
            .apply { color = Color.BLACK }

        val theta = if (link.dhParam.theta.absoluteValue > 10) {
            CSG.unionAll(Extrude.revolve(thetaProfile, 0.0, link.dhParam.theta.absoluteValue, 10))
        } else {
            thetaProfile
        }

        return immutableSetOf(
            theta
                .let { if (link.dhParam.theta < 0) it.rotz(link.dhParam.theta) else it }
                .difference(start) // So they dont overlap with the fan
                .difference(end)   // So they dont overlap with the fan
                .apply { color = Color.AQUA },
            start,
            end
        )
    }

    /**
     * Shows the value of [DhParam.r].
     *
     * @param link The link.
     * @return A CSG showing r.
     */
    private fun getRViz(link: Link): CSG =
        Cube(
            if (link.dhParam.r == 0.0) lengthForParamZero else link.dhParam.r,
            cuboidThickness,
            cuboidThickness
        ).toCSG()
            .toXMax()
            .moveByDhParam(immutableListOf(link.dhParam), false)
            .apply { color = Color.RED }

    /**
     * Shows the value of [DhParam.alpha]. alpha=0 is a straight line, alpha=90 is a quarter
     * circle, etc.
     *
     * @param link The link.
     * @return A CSG showing alpha.
     */
    private fun getAlphaViz(link: Link): ImmutableSet<CSG> {
        val alphaProfile = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()

        val start = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .roty(90)
            .rotx(link.dhParam.alpha) // Rotate back to the start (alpha=0)
            .moveByDhParam(immutableListOf(link.dhParam), false)
            .apply { color = Color.WHITE }

        val end = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .roty(90)
            .moveByDhParam(immutableListOf(link.dhParam), false)
            .apply { color = Color.BLACK }

        val alpha = if (link.dhParam.alpha.absoluteValue > 10) {
            CSG.unionAll(Extrude.revolve(alphaProfile, 0.0, link.dhParam.alpha.absoluteValue, 10))
        } else {
            alphaProfile
        }

        return immutableSetOf(
            alpha
                .roty(90)
                .let { if (link.dhParam.alpha > 0) it.rotx(link.dhParam.alpha) else it }
                .moveByDhParam(immutableListOf(link.dhParam), false)
                .difference(start) // So they dont overlap with the fan
                .difference(end)   // So they dont overlap with the fan
                .apply { color = Color.YELLOW },
            start,
            end
        )
    }

    /**
     * Shows the values of the joint limits.
     *
     * @param link The link.
     * @return A CSG showing the joint limits.
     */
    private fun getLimitsViz(link: Link): ImmutableSet<CSG> {
        val limitsProfile = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .toZMax() // Other side compared to the theta fan so they dont intersect

        // Start shows joint maximum
        val start = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .rotz(link.dhParam.theta + link.jointLimits.maximum)
            .toZMax() // Other side compared to the theta fan so they dont intersect
            .apply { color = Color.WHITE }

        // End shows joint minimum
        val end = Cube(fanRadius, 1.0, 1.0)
            .toCSG()
            .toXMin()
            .rotz(link.dhParam.theta + link.jointLimits.minimum)
            .toZMax() // Other side compared to the theta fan so they dont intersect
            .apply { color = Color.BLACK }

        val limits = if (link.jointLimits.range.absoluteValue > 10) {
            CSG.unionAll(
                Extrude.revolve(
                    limitsProfile,
                    0.0,
                    link.jointLimits.range.absoluteValue,
                    10
                )
            )
        } else {
            limitsProfile
        }

        return immutableSetOf(
            limits
                .rotz(link.dhParam.theta + link.jointLimits.minimum)
                .difference(start) // So they dont overlap with the fan
                .difference(end)   // So they dont overlap with the fan
                .apply { color = Color.LIGHTGREEN },
            start,
            end
        )
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
            baseTransform: FrameTransformation,
            linkTransforms: List<FrameTransformation>,
            frameTransforms: List<FrameTransformation>
        ) {
            val dhTransformList = mutableListOf<FrameTransformation>()
            // Start at the base transform (where the limb attaches to the base)
            var transform = baseTransform
            for (i in 0 until linkTransforms.size) {
                val dhTransform = if (i == 0) {
                    // First link has no transform relative to the base (it is the base)
                    FrameTransformation.identity
                } else {
                    // First link has no transform relative to the base (it is the base)
                    linkTransforms[i - 1]
                }

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
        ): CSG = transformed(
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
