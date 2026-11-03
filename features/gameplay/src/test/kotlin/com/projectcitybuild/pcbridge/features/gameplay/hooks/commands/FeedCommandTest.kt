package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class FeedCommandTest {
    @Test
    fun `restores the target's hunger and saturation`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = FeedCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "feed Alex")

        verify(harness.target).foodLevel = 20
        verify(harness.target).saturation = 20f
        verify(harness.sender).sendMessage(l10n.gameplay.playerFed("Alex"))
    }
}
