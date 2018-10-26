/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.neuronrobotics.sdk.addons.kinematics.DHChain
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder

internal class DefaultDhClassifier : DhClassifier {

    override fun deriveEulerAngles(chain: DHChain): Either<ClassifierError, RotationOrder> {
        TODO("not implemented")
    }
}
