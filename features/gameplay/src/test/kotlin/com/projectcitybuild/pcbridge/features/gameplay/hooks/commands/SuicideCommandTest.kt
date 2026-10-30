package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class SuicideCommandTest {
    @Test
    fun `kills the sender and reports their death`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = SuicideCommand(harness.plugin, harness.executor)

        harness.execute(command, "suicide")

        verify(harness.sender).health = 0.0
        verify(harness.sender).sendMessage(l10n.gameplay.playerDied)
    }
}
