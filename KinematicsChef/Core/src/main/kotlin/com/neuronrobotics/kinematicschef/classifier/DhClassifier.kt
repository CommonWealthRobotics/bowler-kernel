/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.eulerangle.EulerAngle

internal interface DhClassifier {

    /**
     * Determine the Euler angles for a [SphericalWrist].
     *
     * @param wrist The wrist to classify. The thetas must be specified as offsets.
     * @return The Euler angles or an error.
     */
    fun deriveEulerAngles(wrist: SphericalWrist): Either<ClassifierError, EulerAngle>
}
