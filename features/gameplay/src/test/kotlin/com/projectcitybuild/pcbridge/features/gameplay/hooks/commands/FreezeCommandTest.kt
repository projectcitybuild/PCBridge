package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class FreezeCommandTest {
    @Test
    fun `freezes the target for the requested duration`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = FreezeCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "freeze Alex 7")

        verify(harness.target).freezeTicks = 140
        verify(harness.sender).sendMessage(l10n.gameplay.playerFrozen("Alex", 7))
    }
}
