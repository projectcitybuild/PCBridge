package com.projectcitybuild.pcbridge.features.warnings.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class WarnCommandTest {
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)

    @Test
    fun `uses online player's uuid in warning link`() =
        runTest {
            val player = mock(Player::class.java)
            val id = UUID.randomUUID()
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(server.onlinePlayers).thenReturn(setOf(player))
            whenever(player.name).thenReturn("Steve")
            whenever(player.uniqueId).thenReturn(id)
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(1, RemoteConfigKeyValues(manageBaseUrl = "https://manage/")),
            )
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("warn Steve", source())
            runCurrent()
            advanceUntilIdle()

            val url = "https://manage/warnings/create?uuid=$id"
            verify(sender).sendRichMessage("<gray>Click the link below to create a warning for this player</gray>")
            verify(sender).sendRichMessage("<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>")
        }

    @Test
    fun `uses supplied name when target is offline`() =
        runTest {
            whenever(sender.hasPermission(any<String>())).thenReturn(true)
            whenever(server.onlinePlayers).thenReturn(emptySet())
            whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues()))
            val dispatcher = dispatcher(CoroutineScopeCommandTaskExecutor(this))

            dispatcher.execute("warn Offline", source())
            runCurrent()
            advanceUntilIdle()

            val url = "http://localhost/manage/warnings/create?uuid=Offline"
            verify(sender).sendRichMessage("<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>")
        }

    private fun dispatcher(executor: CoroutineScopeCommandTaskExecutor): CommandDispatcher<CommandSourceStack> {
        val command = WarnCommand(mock(Plugin::class.java), server, remoteConfig, executor)
        return CommandDispatcher<CommandSourceStack>().also { it.root.addChild(command.literal()) }
    }

    private fun source(): CommandSourceStack =
        mock(CommandSourceStack::class.java).also { source ->
            whenever(source.sender).thenReturn(sender)
        }
}
