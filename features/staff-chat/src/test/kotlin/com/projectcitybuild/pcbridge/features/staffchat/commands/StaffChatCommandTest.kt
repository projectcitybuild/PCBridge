package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.staffchat.StaffChatPermissions
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val decorators = mock(ChatDecoratorChain::class.java)

    @Test
    fun `sends decorated staff chat only to players with channel permission`() =
        runTest {
            val source = mock(CommandSourceStack::class.java)
            whenever(source.sender).thenReturn(sender)
            whenever(sender.name).thenReturn("moderator")
            whenever(sender.hasPermission(StaffChatPermissions.CHANNEL.node)).thenReturn(true)
            whenever(authorizedRecipient.hasPermission(StaffChatPermissions.CHANNEL.node)).thenReturn(true)
            whenever(unauthorizedRecipient.hasPermission(StaffChatPermissions.CHANNEL.node)).thenReturn(false)
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
            val command = command(this)

            execute(command, "a hello staff", source)
            advanceUntilIdle()

            verify(decorators).pipe(ChatMessage(sender, Component.text("hello staff")))
            verify(sender).sendMessage(any<Component>())
            verify(authorizedRecipient).sendMessage(any<Component>())
            verify(unauthorizedRecipient, never()).sendMessage(any<Component>())
        }

    private fun command(scope: CoroutineScope) =
        StaffChatCommand(
            plugin = mock(Plugin::class.java),
            server = server,
            remoteConfig = remoteConfig,
            decorators = decorators,
            commandTaskExecutor = CoroutineScopeCommandTaskExecutor(scope),
        )

    private fun execute(
        command: BrigadierCommand,
        input: String,
        source: CommandSourceStack,
    ) {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
    }
}
