package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class KillCommandTest {
    @Test
    fun `kills the target and notifies both players`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = KillCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "kill Alex")

        verify(harness.target).health = 0.0
        verify(harness.sender).sendMessage(l10n.gameplay.playerKilled("Alex"))
        verify(harness.target).sendMessage(l10n.gameplay.killedBy("Notch"))
    }
}
