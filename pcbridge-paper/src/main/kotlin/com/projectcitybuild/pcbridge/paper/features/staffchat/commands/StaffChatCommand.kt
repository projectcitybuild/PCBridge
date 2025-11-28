package com.projectcitybuild.pcbridge.paper.features.staffchat.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.staffchat.staffChatTracer
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
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("a")
            .requiresPermission(PermissionNode.STAFF_CHANNEL)
            .then(
                Commands.argument("message", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(staffChatTracer) {
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
