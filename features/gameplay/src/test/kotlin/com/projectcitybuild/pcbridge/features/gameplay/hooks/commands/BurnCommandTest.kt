package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class BurnCommandTest {
    @Test
    fun `sets the target on fire for the requested duration`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = BurnCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "burn Alex 7")

        verify(harness.target).fireTicks = 140
        verify(harness.sender).sendMessage(l10n.gameplay.playerSetOnFire("Alex"))
    }
}
