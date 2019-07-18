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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link.model

import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import helios.json

@json
data class DhParamData(
    val d: Double,
    val theta: Double,
    val r: Double,
    val alpha: Double
) {
    constructor(d: Number, theta: Number, r: Number, alpha: Number) : this(
        d.toDouble(),
        theta.toDouble(),
        r.toDouble(),
        alpha.toDouble()
    )

    constructor(dhParam: DhParam) : this(
        dhParam.d,
        dhParam.theta,
        dhParam.r,
        dhParam.alpha
    )

    fun toDhParam() = DhParam(d, theta, r, alpha)

    companion object
}
