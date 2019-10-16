package com.neuronrobotics.bowlerkernel.kinematics.motion

import com.neuronrobotics.bowlerkernel.kinematics.randomFrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.seaArmLinks
import com.neuronrobotics.bowlerkernel.util.JointLimits
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

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
