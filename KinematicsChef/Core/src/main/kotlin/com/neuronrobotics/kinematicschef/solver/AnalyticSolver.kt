/*
 * Copyright 2018 Jason McKinney
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.solver

import com.neuronrobotics.sdk.addons.kinematics.DHChain
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR

interface AnalyticSolver {
    /**
     * Calculates joint angles for a given system
     *
     * @param target The target frame transformation
     * @param jointSpaceVector The current joint angles
     * @param chain The DH parameter chain of the system
     * @return The joint angles that best meet the target
     */
    fun solve(target: TransformNR, jointSpaceVector: DoubleArray, chain: DHChain): DoubleArray;
}