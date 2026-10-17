package com.projectcitybuild.pcbridge.features.warnings.commands

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class WarnCommandTest {
    private val context = mock(PaperCommandContext::class.java)
    private val sender = mock(CommandSender::class.java)
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val command = WarnCommand(mock(Plugin::class.java), server, remoteConfig)

    @Test
    fun `uses online player's uuid in warning link`() =
        runTest {
            val player = mock(org.bukkit.entity.Player::class.java)
            val id = UUID.randomUUID()
            val source = mock(CommandSourceStack::class.java)
            whenever(context.input).thenReturn("/warn Steve")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)
            whenever(context.getArgument("player", String::class.java)).thenReturn("Steve")
            whenever(server.onlinePlayers).thenReturn(setOf(player))
            whenever(player.name).thenReturn("Steve")
            whenever(player.uniqueId).thenReturn(id)
            whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues(manageBaseUrl = "https://manage/")))

            command.execute(context)

            val url = "https://manage/warnings/create?uuid=$id"
            verify(sender).sendRichMessage("<gray>Click the link below to create a warning for this player</gray>")
            verify(sender).sendRichMessage("<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>")
        }

    @Test
    fun `uses supplied name when target is offline`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(context.input).thenReturn("/warn Offline")
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)
            whenever(context.getArgument("player", String::class.java)).thenReturn("Offline")
            whenever(server.onlinePlayers).thenReturn(emptySet())
            whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues()))

            command.execute(context)

            val url = "http://localhost/manage/warnings/create?uuid=Offline"
            verify(sender).sendRichMessage("<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>")
        }
}
