/*
 * This file is part of kinematics-chef.
 *
 * kinematics-chef is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kinematics-chef is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with kinematics-chef.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist

/**
 * Identifies common spherical wrist configurations.
 */
@SuppressWarnings("ComplexMethod")
class DefaultWristIdentifier : WristIdentifier {

    override fun isSphericalWrist(chain: ImmutableList<DhParam>): Option<String> {
        return if (chain.size == 3) {
            fun config1() = chain[0].alpha == -90.0 && chain[1].alpha == 90.0
            fun config2() = chain[0].alpha == 90.0 && chain[1].alpha == -90.0
            fun config3() = chain[0].alpha == 0.0 && chain[1].alpha == 90.0 &&
                chain[2].alpha == -90.0

            fun config4() = chain[0].alpha == 0.0 && chain[1].alpha == -90.0 &&
                chain[2].alpha == 90.0

            fun centerLinkNoOffset() = chain[1].r == 0.0 && chain[1].d == 0.0

            @SuppressWarnings("ComplexCondition")
            if ((config1() || config2() || config3() || config4()) && centerLinkNoOffset()) {
                Option.empty()
            } else {
                Option.just("Not spherical.")
            }
        } else {
            Option.just(
                "A chain of ${chain.size} links cannot form a spherical wrist"
            )
        }
    }

    override fun isSphericalWrist(
        chain: ImmutableList<DhParam>,
        priorChain: ImmutableList<DhParam>,
        inverseTipTransform: FrameTransformation
    ): Either<String, ImmutableList<DhParam>> {
        return if (chain.size == 3) {
            isSphericalWrist(chain).fold(
                { Either.right(chain) },
                {
                    val wristCenter = FrameTransformation.fromTranslation(
                        SphericalWrist(chain).centerHomed(priorChain)
                    )

                    val position = wristCenter * inverseTipTransform
                    if (position.translationY == 0.0 && position.translationZ == 0.0) {
                        // The center of the wrist lies on the x-axis so it is spherical
                        TODO("Not implemented")
                    } else {
                        // The center of the wrist does not lie on the x-axis so it cannot be spherical
                        Either.left("Not spherical.")
                    }
                }
            )
        } else {
            Either.left("A chain of ${chain.size} links cannot form a spherical wrist")
        }
    }
}
