package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.SimplePaginator
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PaginationBuilder
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpListCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(
                Commands.argument("page", IntegerArgumentType.integer(1))
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val sender = context.source.sender

        val warps = warpRepository.all()
        val page = SimplePaginator<Warp>().paginate(
            items = warps,
            pageSize = remoteConfig.latest.config.warps.itemsPerPage,
            page = pageNumber,
        )
        if (page.items.isEmpty()) {
            sender.sendRichMessage("<gray>No warps found</gray>")
            return@traceSuspending
        }
        val message = PaginationBuilder().build(
            title = "Warps",
            items = page.items,
            pageNumber = pageNumber,
            totalPages = page.totalPages,
            pageCommand = { index -> "/warps list $index" },
            itemClickCommand = { "/warp ${it.name}" },
            itemHover = { "Teleport to ${it.name}" },
            itemDecorator = {
                "<gray>\"<aqua><u>${it.name}</u></aqua>\" (${it.world})</gray>"
            },
        )
        sender.sendMessage(message)
    }
}
