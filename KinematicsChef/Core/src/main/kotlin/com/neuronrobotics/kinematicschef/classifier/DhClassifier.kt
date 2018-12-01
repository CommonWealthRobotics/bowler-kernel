/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle
import org.ejml.simple.SimpleMatrix

internal interface DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify.
     * @param priorChain The links in the chain before the [wrist].
     * @param tipTransform The frame transformation for the tip of the chain.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(
        wrist: SphericalWrist,
        priorChain: ImmutableList<DhParam>,
        tipTransform: SimpleMatrix
    ): Either<ClassifierError, EulerAngle>
}
