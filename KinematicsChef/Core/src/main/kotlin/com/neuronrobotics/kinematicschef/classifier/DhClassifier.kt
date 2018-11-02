/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder

internal interface DhClassifier {

    /**
     * Determine the Euler angles for a [DHChain]. Returns an error for non-spherical wrists.
     * This is meant to be called with each potential spherical wrist in a chain.
     *
     * @param wrist The wrist to classify.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(wrist: SphericalWrist): Either<ClassifierError, RotationOrder>
}
