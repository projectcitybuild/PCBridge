package com.projectcitybuild.pcbridge.paper.features.staffchat.commands

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessageDecorator
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.architecture.permissions.hasPermission
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ACommandTest {
    private lateinit var plugin: Plugin
    private lateinit var server: Server
    private lateinit var remoteConfig: RemoteConfig
    private lateinit var decoratorChain: ChatDecoratorChain

    @BeforeEach
    fun setUp() {
        plugin = mock(Plugin::class.java)
        server = mock(Server::class.java)
        remoteConfig = mock(RemoteConfig::class.java)
        decoratorChain = ChatDecoratorChain()
    }

    @Test
    fun `only players with permission receive staff messages`() =
        runTest {
            val regularPlayer =
                mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(false)
                }
            val staffPlayer =
                mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(true)
                }
            whenever(server.onlinePlayers).thenReturn(
                listOf(regularPlayer, staffPlayer),
            )
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config = RemoteConfigKeyValues(
                        chat = RemoteConfigKeyValues.Chat(staffChannel = "staff: <name> <message>")
                    ),
                )
            )

            val sender = mock(Player::class.java)
            whenever(sender.name).thenReturn("user123")

            val context: PaperCommandContext = mock()
            val source = mock(CommandSourceStack::class.java)
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(sender)
            whenever(context.getArgument("message", String::class.java)).thenReturn("foo bar")

            StaffChatCommand(plugin, server, remoteConfig, decoratorChain).execute(context)

            argumentCaptor<Component>().apply {
                verify(staffPlayer).sendMessage(capture())
                assertTrue(firstValue.toString().contains("staff: user123 foo bar"))
            }
            verify(regularPlayer, never()).sendMessage(any<Component>())
        }

    @Test
    fun `decorates messages`() =
        runTest {
            decoratorChain.messages(object : ChatMessageDecorator {
                override suspend fun decorate(prev: ChatMessage): ChatMessage {
                    return prev.copy(message = Component.text("replaced_message"))
                }
            })

            val staffPlayer =
                mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(true)
                }
            whenever(server.onlinePlayers).thenReturn(listOf(staffPlayer))
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config = RemoteConfigKeyValues(
                        chat = RemoteConfigKeyValues.Chat(staffChannel = "staff: <name> <message>")
                    ),
                )
            )

            whenever(staffPlayer.name).thenReturn("user123")

            val context: PaperCommandContext = mock()
            val source = mock(CommandSourceStack::class.java)
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(staffPlayer)
            whenever(context.getArgument("message", String::class.java)).thenReturn("foo bar")

            StaffChatCommand(plugin, server, remoteConfig, decoratorChain).execute(context)

            argumentCaptor<Component>().apply {
                verify(staffPlayer).sendMessage(capture())
                assertTrue(firstValue.toString().contains("staff: user123 replaced_message"))
            }
        }
}
