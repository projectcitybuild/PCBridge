package com.projectcitybuild.pcbridge.features.staffchat.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.features.staffchat.staffChatTracer
import io.papermc.paper.command.brigadier.Commands
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
    override fun literal(): PaperCommandNode {
        return Commands.literal("a")
            .requiresPermission(PermissionNode.STAFF_CHANNEL)
            .then(
                Commands.argument("message", StringArgumentType.greedyString())
                    .executesSuspending(commandTaskExecutor, ::execute)
            )
            .build()
    }

    suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(staffChatTracer) {
        val player = context.source.requirePlayer()
        val rawMessage = context.getArgument("message", String::class.java)

        val format = remoteConfig.latest.config.chat.staffChannel
        val decoratedMessage = decorators.pipe(
            ChatMessage(player, Component.text(rawMessage))
        )
        val message = MiniMessage.miniMessage().deserialize(
            format,
            Placeholder.component("name", Component.text(player.name)),
            Placeholder.component("message", decoratedMessage.message),
        )

        server.onlinePlayers
            .filter { it.hasPermission(PermissionNode.STAFF_CHANNEL.node) }
            .forEach { it.sendMessage(message) }
    }
}
