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
package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.neuronrobotics.bowlerkernel.kinematics.randomFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import com.neuronrobotics.bowlerkernel.util.JointLimits
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import java.lang.IllegalStateException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class LengthAndIKBasedReachabilityCalculatorTest {

    @Test
    fun `both methods return true`() {
        val ft = randomFrameTransformation()
        val links = seaArmLinks
        val jointLimits = links.map { JointLimits.NoLimit }

        val mockCalc = mockk<LengthBasedReachabilityCalculator> {
            every { isFrameTransformationReachable(ft, links, any()) } returns true
        }

        val mockIK = mockk<InverseKinematicsSolver> {
            every { solveChain(links, any(), jointLimits, ft) } returns links.map { 0.0 }
        }

        val calc = LengthAndIKBasedReachabilityCalculator(mockIK, mockCalc)
        assertTrue(calc.isFrameTransformationReachable(ft, links, jointLimits))

        verifyOrder {
            mockCalc.isFrameTransformationReachable(ft, links, any())
            mockIK.solveChain(links, any(), jointLimits, ft)
        }

        confirmVerified(mockCalc, mockIK)
    }

    @Test
    fun `calc returns false`() {
        val ft = randomFrameTransformation()
        val links = seaArmLinks
        val jointLimits = links.map { JointLimits.NoLimit }

        val mockCalc = mockk<LengthBasedReachabilityCalculator> {
            every { isFrameTransformationReachable(ft, links, any()) } returns false
        }

        val mockIK = mockk<InverseKinematicsSolver> {}

        val calc = LengthAndIKBasedReachabilityCalculator(mockIK, mockCalc)
        assertFalse(calc.isFrameTransformationReachable(ft, links, jointLimits))

        verifyOrder {
            mockCalc.isFrameTransformationReachable(ft, links, any())
        }

        confirmVerified(mockCalc, mockIK)
    }

    @Test
    fun `ik blows up`() {
        val ft = randomFrameTransformation()
        val links = seaArmLinks
        val jointLimits = links.map { JointLimits.NoLimit }

        val mockCalc = mockk<LengthBasedReachabilityCalculator> {
            every { isFrameTransformationReachable(ft, links, any()) } returns true
        }

        val mockIK = mockk<InverseKinematicsSolver> {
            every { solveChain(links, any(), jointLimits, ft) } throws IllegalStateException()
        }

        val calc = LengthAndIKBasedReachabilityCalculator(mockIK, mockCalc)
        assertFalse(calc.isFrameTransformationReachable(ft, links, jointLimits))

        verifyOrder {
            mockCalc.isFrameTransformationReachable(ft, links, any())
            mockIK.solveChain(links, any(), jointLimits, ft)
        }

        confirmVerified(mockCalc, mockIK)
    }
}
