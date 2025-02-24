package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.LengthAwarePaginator
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin

class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val page = context.getOptionalArgument("page", Int::class.java) ?: 1
        val builds = buildRepository.all(page)
        val sender = context.source.sender

        sender.sendMessage(
            LengthAwarePaginator().component(
                title = "Build List",
                paginatedData = builds,
                pageCommandBuilder = { pageIndex -> "/homes list $pageIndex" },
                itemDecorator = { build ->
                    val text = "<gray>#${build.id} \"<aqua>${build.name}</aqua>\" (<white>${build.votes}</white> votes)</gray>"

                    MiniMessage.miniMessage().deserialize(text)
                        // Separate handling here to ensure character escaping in the name
                        .clickEvent(ClickEvent.runCommand("/build ${build.name}"))
                        .hoverEvent(HoverEvent.showText(Component.text("Teleport to ${build.name}")))
                },
            )
        )
    }
}