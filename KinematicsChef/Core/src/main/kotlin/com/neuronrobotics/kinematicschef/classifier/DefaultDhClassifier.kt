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
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import javax.inject.Inject

internal class DefaultDhClassifier
@Inject internal constructor(
    private val wristIdentifier: WristIdentifier
) : DhClassifier {

    override fun deriveEulerAngles(chain: ImmutableList<DhParam>): Either<ClassifierError, RotationOrder> {
        if (wristIdentifier.isSphericalWrist(chain)) {
            TODO("not implemented")
        } else {
            return Either.left(ClassifierError("The wrist is not spherical."))
        }
    }
}
