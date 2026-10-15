package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Player
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessageDecorator
import com.projectcitybuild.pcbridge.runtime.permissions.hasPermission
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.sun.security.ntlm.Server
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.awt.Component
import kotlin.jvm.java

class ACommandTest {
    private lateinit var plugin: Plugin
    private lateinit var server: Server
    private lateinit var remoteConfig: RemoteConfig
    private lateinit var decoratorChain: ChatDecoratorChain

    @BeforeEach
    fun setUp() {
        plugin = Mockito.mock(Plugin::class.java)
        server = Mockito.mock(Server::class.java)
        remoteConfig = Mockito.mock(RemoteConfig::class.java)
        decoratorChain = ChatDecoratorChain()
    }

    @Test
    fun `only players with permission receive staff messages`() =
        runTest {
            val regularPlayer =
                Mockito.mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(false)
                }
            val staffPlayer =
                Mockito.mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(true)
                }
            whenever(server.onlinePlayers).thenReturn(
                listOf(regularPlayer, staffPlayer),
            )
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config =
                        RemoteConfigKeyValues(
                            chat = RemoteConfigKeyValues.Chat(staffChannel = "staff: <name> <message>"),
                        ),
                ),
            )

            val sender = Mockito.mock(Player::class.java)
            whenever(sender.name).thenReturn("user123")

            val context: PaperCommandContext = Mockito.mock()
            val source = Mockito.mock(CommandSourceStack::class.java)
            whenever(context.source).thenReturn(source)
            whenever(context.input).thenReturn("a")
            whenever(source.sender).thenReturn(sender)
            whenever(context.getArgument("message", String::class.java)).thenReturn("foo bar")

            StaffChatCommand(plugin, server, remoteConfig, decoratorChain).execute(context)

            argumentCaptor<Component>().apply {
                verify(staffPlayer).sendMessage(KArgumentCaptor.capture())
                Assertions.assertTrue(KArgumentCaptor.firstValue.toString().contains("staff: user123 foo bar"))
            }
            verify(regularPlayer, never()).sendMessage(ArgumentMatchers.any<Component>())
        }

    @Test
    fun `decorates messages`() =
        runTest {
            decoratorChain.messages(
                object : ChatMessageDecorator {
                    override suspend fun decorate(prev: ChatMessage): ChatMessage {
                        return prev.copy(message = Component.text("replaced_message"))
                    }
                },
            )

            val staffPlayer =
                Mockito.mock(Player::class.java).also {
                    whenever(it.hasPermission(PermissionNode.STAFF_CHANNEL)).thenReturn(true)
                }
            whenever(server.onlinePlayers).thenReturn(listOf(staffPlayer))
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config =
                        RemoteConfigKeyValues(
                            chat = RemoteConfigKeyValues.Chat(staffChannel = "staff: <name> <message>"),
                        ),
                ),
            )

            whenever(staffPlayer.name).thenReturn("user123")

            val context: PaperCommandContext = Mockito.mock()
            val source = Mockito.mock(CommandSourceStack::class.java)
            whenever(context.source).thenReturn(source)
            whenever(context.input).thenReturn("a")
            whenever(source.sender).thenReturn(staffPlayer)
            whenever(context.getArgument("message", String::class.java)).thenReturn("foo bar")

            StaffChatCommand(plugin, server, remoteConfig, decoratorChain).execute(context)

            argumentCaptor<Component>().apply {
                verify(staffPlayer).sendMessage(KArgumentCaptor.capture())
                Assertions.assertTrue(KArgumentCaptor.firstValue.toString().contains("staff: user123 replaced_message"))
            }
        }
}