package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PurgeCommandTest {
    @Test
    fun `reports when the target has no active potion effects`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.target.activePotionEffects).thenReturn(emptySet())
        val command = PurgeCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "purge Alex")

        verify(harness.sender).sendMessage(l10n.gameplay.noActivePotionEffects)
    }
}
