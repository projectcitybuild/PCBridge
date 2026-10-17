package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class StaffChatCommandTest {
    private val sender = mock(Player::class.java)
    private val authorizedRecipient = mock(Player::class.java)
    private val unauthorizedRecipient = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val decorators = mock(ChatDecoratorChain::class.java)
    private val command =
        StaffChatCommand(
            plugin = mock(Plugin::class.java),
            server = server,
            remoteConfig = remoteConfig,
            decorators = decorators,
        )

    @Test
    fun `sends decorated staff chat only to players with channel permission`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(context.source).thenReturn(source)
            whenever(context.input).thenReturn("/a hello staff")
            whenever(context.getArgument("message", String::class.java)).thenReturn("hello staff")
            whenever(source.sender).thenReturn(sender)
            whenever(sender.name).thenReturn("moderator")
            whenever(sender.hasPermission(PermissionNode.STAFF_CHANNEL.node)).thenReturn(true)
            whenever(authorizedRecipient.hasPermission(PermissionNode.STAFF_CHANNEL.node)).thenReturn(true)
            whenever(unauthorizedRecipient.hasPermission(PermissionNode.STAFF_CHANNEL.node)).thenReturn(false)
            whenever(server.onlinePlayers).thenReturn(
                setOf(sender, authorizedRecipient, unauthorizedRecipient),
            )
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config =
                        RemoteConfigKeyValues(
                            chat =
                                RemoteConfigKeyValues.Chat(
                                    staffChannel = "<yellow>(Staff) <name>:</yellow> <message>",
                                ),
                        ),
                ),
            )
            whenever(decorators.pipe(any<ChatMessage>()))
                .thenReturn(ChatMessage(sender, Component.text("decorated message")))

            command.execute(context)

            verify(decorators).pipe(ChatMessage(sender, Component.text("hello staff")))
            verify(sender).sendMessage(any<Component>())
            verify(authorizedRecipient).sendMessage(any<Component>())
            verify(unauthorizedRecipient, never()).sendMessage(any<Component>())
        }
}
