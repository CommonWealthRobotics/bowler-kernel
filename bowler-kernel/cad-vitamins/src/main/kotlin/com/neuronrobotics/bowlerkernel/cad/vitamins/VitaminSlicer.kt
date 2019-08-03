/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.cad.vitamins

import com.neuronrobotics.bowlerkernel.vitamins.vitamin.CenterOfMass
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Vector3d
import mu.KotlinLogging
import org.octogonapus.ktunits.quantities.millimeter
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

/**
 * Slices a [CSG] to determine its center of mass. Assumes that the [CSG] has a uniform density.
 *
 * @param slicePlaneThickness The thickness of the slice plane [CSG].
 * @param sliceStep The resolution of the slices (distance between slices).
 */
class VitaminSlicer(
    private val sliceStep: Double = 1.0,
    private val slicePlaneThickness: Double = sliceStep
) {

    fun getCenterOfMass(vit: CSG): CenterOfMass {
        val xSlices = sliceOnAxis(
            listOf(vit),
            { maxX - centerX },
            { minX + centerX },
            this::getXAxisSlicePlane
        )
        LOGGER.debug { "xSlices size: ${xSlices.size}" }

        val ySlices = sliceOnAxis(
            xSlices,
            { maxY - centerY },
            { minY + centerY },
            this::getYAxisSlicePlane
        )
        LOGGER.debug { "ySlices size: ${ySlices.size}" }

        val zSlices = sliceOnAxis(
            ySlices,
            { maxZ - centerZ },
            { minZ + centerZ },
            this::getZAxisSlicePlane
        )
        LOGGER.debug { "zSlices size: ${zSlices.size}" }

        val averageCenter = averageCenters(zSlices)

        return CenterOfMass(
            x = averageCenter.x.millimeter,
            y = averageCenter.y.millimeter,
            z = averageCenter.z.millimeter
        )
    }

    private fun averageCenters(slices: List<CSG>): Vector3d {
        val size = AtomicLong(0)
        return slices.parallelStream()
            .filter { it.polygons.isNotEmpty() }
            .map { it.center }
            .peek { size.incrementAndGet() }
            .reduce(Vector3d::plus)
            .get()
            .dividedBy(size.toDouble())
    }

    private fun sliceOnAxis(
        slices: List<CSG>,
        maxDim: CSG.() -> Double,
        minDim: CSG.() -> Double,
        getSlicePlane: (CSG, Double) -> CSG
    ): List<CSG> {
        return slices.parallelStream().flatMap { toSlice ->
            val newSlices = mutableListOf<CSG>()
            val centeredSlice = toSlice.move(toSlice.center.negated())

            var height = 0.0
            while (height >= centeredSlice.minDim()) {
                newSlices += slice(
                    centeredSlice,
                    getSlicePlane(centeredSlice, height)
                ).move(toSlice.center)
                height -= sliceStep
            }

            height = sliceStep // Don't slice at 0 twice
            while (height <= centeredSlice.maxDim()) {
                newSlices += slice(
                    centeredSlice,
                    getSlicePlane(centeredSlice, height)
                ).move(toSlice.center)
                height += sliceStep
            }

            newSlices.parallelStream()
        }.collect(Collectors.toList())
    }

    private fun slice(vit: CSG, slicePlane: CSG): CSG =
        if (vit.polygons.isEmpty()) {
            vit
        } else {
            vit.intersect(slicePlane)
        }

    private fun getXAxisSlicePlane(vit: CSG, height: Double): CSG =
        Cube(
            Vector3d(height, 0.0, 0.0),
            Vector3d(slicePlaneThickness, vit.totalY, vit.totalZ)
        ).toCSG()

    private fun getYAxisSlicePlane(vit: CSG, height: Double): CSG =
        Cube(
            Vector3d(0.0, height, 0.0),
            Vector3d(vit.totalX, slicePlaneThickness, vit.totalZ)
        ).toCSG()

    private fun getZAxisSlicePlane(vit: CSG, height: Double): CSG =
        Cube(
            Vector3d(0.0, 0.0, height),
            Vector3d(vit.totalX, vit.totalY, slicePlaneThickness)
        ).toCSG()

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
