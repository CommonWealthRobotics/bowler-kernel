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

import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import eu.mihosoft.vrl.v3d.Transform
import javafx.scene.transform.Affine
import javafx.scene.transform.MatrixType
import javax.vecmath.Matrix4d

/**
 * Maps a [FrameTransformation] to an [Affine].
 *
 * @return The [Affine] representation.
 */
fun FrameTransformation.affine(): Affine = Affine().also { setAffine(it) }

/**
 * Maps a [FrameTransformation] to an [Affine] and modifies the given [Affine].
 *
 * @param affine The [Affine] to modify.
 */
fun FrameTransformation.setAffine(affine: Affine) {
    rotation.let {
        affine.mxx = it[0, 0]
        affine.mxy = it[0, 1]
        affine.mxz = it[0, 2]
        affine.myx = it[1, 0]
        affine.myy = it[1, 1]
        affine.myz = it[1, 2]
        affine.mzx = it[2, 0]
        affine.mzy = it[2, 1]
        affine.mzz = it[2, 2]
    }

    translation.let {
        affine.tx = it[0, 0]
        affine.ty = it[1, 0]
        affine.tz = it[2, 0]
    }
}

/**
 * Maps a [FrameTransformation] to a [Transform].
 *
 * @return The [Transform] representation.
 */
fun FrameTransformation.toTransform(): Transform = Transform(Matrix4d(data))

/**
 * Maps an [Affine] to a [Transform].
 *
 * @return The [Transform] representation.
 */
fun Affine.toTransform(): Transform = Transform(Matrix4d(toArray(MatrixType.MT_3D_4x4)))
