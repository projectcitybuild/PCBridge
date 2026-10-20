package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.features.staffchat.StaffChatPermissions
import com.projectcitybuild.pcbridge.features.staffchat.staffChatTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class StaffChatCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val remoteConfig: RemoteConfig,
    private val decorators: ChatDecoratorChain,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("a") {
        description = "Sends a message to staff chat"
        requiresPermission(StaffChatPermissions.CHANNEL)
        argument("message", StringArgumentType.greedyString()) {
            executesSuspending(commandTaskExecutor, ::execute)
        }
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(staffChatTracer) {
        val player = context.source.requirePlayer()
        val rawMessage = context.getArgument("message", String::class.java)

        val format = remoteConfig.latest.config.chat.staffChannel
        val decoratedMessage = decorators.pipe(
            ChatMessage(player, Component.text(rawMessage)),
        )
        val message = MiniMessage.miniMessage().deserialize(
            format,
            Placeholder.component("name", Component.text(player.name)),
            Placeholder.component("message", decoratedMessage.message),
        )

        server.onlinePlayers
            .filter { it.hasPermission(StaffChatPermissions.CHANNEL.node) }
            .forEach { it.sendMessage(message) }
    }
}
