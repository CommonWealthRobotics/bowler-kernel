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
package com.neuronrobotics.bowlerkernel.vitamins.vitamin

import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Vector3d
import org.octogonapus.ktunits.quantities.millimeter

/**
 * Slices a [CSG] to determine its center of mass. Assumes that the [CSG] has a uniform density.
 *
 * @param slicePlaneThickness The thickness of the slice plane [CSG].
 * @param sliceStep The resolution of the slices (distance between slices).
 */
class VitaminSlicer(
    private val slicePlaneThickness: Double = 1e-5,
    private val sliceStep: Double = 1.0
) {

    fun getCenterOfMass(vit: CSG): CenterOfMass {
        val xSlices = sliceOnAxis(listOf(vit), CSG::getMaxX, CSG::getMinX, this::getXAxisSlicePlane)
        println(xSlices.size)
        val ySlices = sliceOnAxis(xSlices, CSG::getMaxY, CSG::getMinY, this::getYAxisSlicePlane)
        println(ySlices.size)
        val zSlices = sliceOnAxis(ySlices, CSG::getMaxZ, CSG::getMinZ, this::getZAxisSlicePlane)
        println(zSlices.size)

        val sumOfCenters = zSlices.filter { it.polygons.isNotEmpty() }
            .map { it.center }
            .fold(Vector3d(0.0, 0.0, 0.0), Vector3d::plus)

        val averageCenter = sumOfCenters.dividedBy(zSlices.size.toDouble())

        return CenterOfMass(
            x = averageCenter.x.millimeter,
            y = averageCenter.y.millimeter,
            z = averageCenter.z.millimeter
        )
    }

    private fun sliceOnAxis(
        slices: List<CSG>,
        maxDim: CSG.() -> Double,
        minDim: CSG.() -> Double,
        getSlicePlane: (CSG, Double) -> CSG
    ): List<CSG> {
        val newSlices = mutableListOf<CSG>()

        slices.forEach { toSlice ->
            // TODO: Start slicing from the exact center instead of from the top
            var height = toSlice.maxDim()
            while (true) {
                if (height < toSlice.minDim()) {
                    newSlices += slice(toSlice, getSlicePlane(toSlice, toSlice.minDim()))
                    break
                }

                newSlices += slice(toSlice, getSlicePlane(toSlice, height))
                height -= sliceStep
            }
        }

        return newSlices
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
        ).toCSG().move(vit.center)

    private fun getYAxisSlicePlane(vit: CSG, height: Double): CSG =
        Cube(
            Vector3d(0.0, height, 0.0),
            Vector3d(vit.totalX, slicePlaneThickness, vit.totalZ)
        ).toCSG().move(vit.center)

    private fun getZAxisSlicePlane(vit: CSG, height: Double): CSG =
        Cube(
            Vector3d(0.0, 0.0, height),
            Vector3d(vit.totalX, vit.totalY, slicePlaneThickness)
        ).toCSG().move(vit.center)
}
