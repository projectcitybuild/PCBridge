package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PaginationBuilder
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

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
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val builds = buildRepository.all(pageNumber)
        val totalPages = ceil(builds.total.toDouble() / builds.perPage.toDouble()).toInt()
        val miniMessage = MiniMessage.miniMessage()
        val sender = context.source.sender

        if (builds.data.isEmpty()) {
            sender.sendMessage(
                miniMessage.deserialize("<gray>No builds available</gray>")
            )
            return@traceSuspending
        }
        val message = PaginationBuilder().build(
            title = "Build List",
            items = builds.data,
            pageNumber = pageNumber,
            totalPages = totalPages,
            pageCommand = { index -> "/builds list $index" },
            itemClickCommand = { "/build ${it.name}" },
            itemHover = { "Teleport to ${it.name}" },
            itemDecorator = {
                "<gray>#${it.id} \"<aqua>${it.name}</aqua>\" (<white>${it.votes}</white> votes)</gray>"
            },
        )
        sender.sendMessage(message)
    }
}