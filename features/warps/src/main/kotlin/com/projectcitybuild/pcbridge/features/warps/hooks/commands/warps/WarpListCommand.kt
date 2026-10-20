package com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import org.bukkit.plugin.Plugin

class WarpListCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("list") {
        description = "Lists available warps"
        requiresPermission(WarpsPermissions.TELEPORT)
        argument("page", IntegerArgumentType.integer(1)) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val sender = context.source.sender

        val warps =
            warpRepository.all(
                page = pageNumber,
                size = remoteConfig.latest.config.warps.itemsPerPage,
            )
        if (warps.data.isEmpty()) {
            sender.sendMessage(
                if (pageNumber == 1) {
                    l10n.warps.noWarpsFound
                } else {
                    l10n.pagination.errorPageNotFound
                },
            )
            return@scoped
        }
        val message =
            PageComponentBuilder().buildWithComponents(
                title = l10n.warps.warpsListTitle,
                paginated = warps,
                pageCommand = { index -> "/warps list $index" },
                itemClickCommand = { "/warp ${it.name}" },
                itemHover = { l10n.lists.teleportTo(it.name) },
                itemDecorator = { l10n.warps.warpListItem(it.name, it.world) },
            )
        sender.sendMessage(message)
    }
}
