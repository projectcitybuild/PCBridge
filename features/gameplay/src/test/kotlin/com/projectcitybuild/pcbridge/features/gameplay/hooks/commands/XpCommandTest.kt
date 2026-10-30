package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

class XpCommandTest {
    @Test
    fun `sets the target's experience points`() = runTest {
        val harness = harness(this, initialExperience = 40)

        harness.execute(command(harness), "xp set 20 Alex")

        verify(harness.target).exp = 0f
        verify(harness.target).level = 0
        verify(harness.target).totalExperience = 0
        verify(harness.target).giveExp(20)
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceSet("Alex", 20))
    }

    @Test
    fun `adds experience points to the target`() = runTest {
        val harness = harness(this, initialExperience = 10)

        harness.execute(command(harness), "xp add 5 Alex")

        verify(harness.target).giveExp(5)
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceAdded("Alex", 5, 15))
    }

    @Test
    fun `subtracts experience points from the target`() = runTest {
        val harness = harness(this, initialExperience = 10)

        harness.execute(command(harness), "xp subtract 3 Alex")

        verify(harness.target).giveExp(-3)
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceSubtracted("Alex", 3, 7))
    }

    @Test
    fun `resets the target's experience points`() = runTest {
        val harness = harness(this, initialExperience = 40)

        harness.execute(command(harness), "xp reset Alex")

        verify(harness.target).exp = 0f
        verify(harness.target).level = 0
        verify(harness.target).totalExperience = 0
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceReset("Alex"))
    }

    @Test
    fun `legacy syntax still adds experience points`() = runTest {
        val harness = harness(this, initialExperience = 2)

        harness.execute(command(harness), "xp 12 Alex")

        verify(harness.target).giveExp(12)
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceAdded("Alex", 12, 14))
    }

    @Test
    fun `negative legacy amount reports subtraction and resulting experience`() = runTest {
        val harness = harness(this, initialExperience = 10)

        harness.execute(command(harness), "xp -3 Alex")

        verify(harness.target).giveExp(-3)
        verify(harness.sender).sendMessage(l10n.gameplay.playerExperienceSubtracted("Alex", 3, 7))
    }

    private fun command(harness: GameplayCommandTestHarness) = XpCommand(harness.plugin, harness.server, harness.executor)

    private fun harness(
        scope: TestScope,
        initialExperience: Int = 0,
    ): GameplayCommandTestHarness {
        val harness = GameplayCommandTestHarness(scope)
        var currentExperience = initialExperience
        whenever(harness.target.totalExperience).thenAnswer { currentExperience }
        doAnswer { invocation ->
            currentExperience = invocation.getArgument<Int>(0)
            null
        }.whenever(harness.target).totalExperience = any()
        doAnswer { invocation ->
            currentExperience = (currentExperience + invocation.getArgument<Int>(0)).coerceAtLeast(0)
            null
        }.whenever(harness.target).giveExp(any())
        return harness
    }
}
