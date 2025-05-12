package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PaginationBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.HOMES_USE)
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val homes = homeRepository.all(playerUUID = sender.uniqueId, page = pageNumber)

        if (homes.data.isEmpty()) {
            sender.sendRichMessage("<gray>No builds available</gray>")
            return@traceSuspending
        }
        val message = PaginationBuilder().build(
            title = "Your Homes",
            items = homes.data,
            pageNumber = homes.currentPage,
            totalPages = homes.total,
            pageCommand = { index -> "/homes list $index" },
            itemClickCommand = { "/home ${it.name}" },
            itemHover = { "Teleport to ${it.name}" },
            itemDecorator = {
                "<gray>#${it.id} \"<aqua>${it.name}</aqua>\"</gray>"
            },
        )
        sender.sendMessage(message)
    }
}