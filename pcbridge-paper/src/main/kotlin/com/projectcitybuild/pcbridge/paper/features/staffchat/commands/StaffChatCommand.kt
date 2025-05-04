package com.projectcitybuild.pcbridge.paper.features.staffchat.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class StaffChatCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val remoteConfig: RemoteConfig,
    private val decorators: ChatDecoratorChain,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("a")
            .requiresPermission(PermissionNode.STAFF_CHANNEL)
            .then(
                Commands.argument("message", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val rawMessage = context.getArgument("message", String::class.java)

        val format = remoteConfig.latest.config.chat.staffChannel
        val decoratedMessage = decorators.pipe(
            ChatMessage(sender, Component.text(rawMessage))
        )
        val message = MiniMessage.miniMessage().deserialize(
            format,
            Placeholder.component("name", Component.text(sender.name)),
            Placeholder.component("message", decoratedMessage.message),
        )

        server.onlinePlayers
            .filter { it.hasPermission(PermissionNode.STAFF_CHANNEL.node) }
            .forEach { it.sendMessage(message) }
    }
}
