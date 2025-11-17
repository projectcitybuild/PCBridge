package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpListCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(
                Commands.argument("page", IntegerArgumentType.integer(1))
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val sender = context.source.sender


        val warps = warpRepository.all(
            page = pageNumber,
            size = remoteConfig.latest.config.warps.itemsPerPage,
        )
        if (warps.data.isEmpty()) {
            sender.sendRichMessage(
                if (pageNumber == 1) l10n.noWarpsFound
                else l10n.errorPageNotFound
            )
            return@scopedSuspending
        }
        val message = PageComponentBuilder().build(
            title = "Warps",
            paginated = warps,
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
