package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.bukkit.GameMode
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify

class GameModeCommandTest {
    @Test
    fun `sets the target's game mode`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = GameModeCommand(harness.plugin, harness.server, harness.executor)

        harness.execute(command, "gamemode creative Alex")

        verify(harness.target).gameMode = GameMode.CREATIVE
        verify(harness.sender).sendMessage(l10n.gameplay.playerGameModeChanged("Alex", "creative"))
    }
}
