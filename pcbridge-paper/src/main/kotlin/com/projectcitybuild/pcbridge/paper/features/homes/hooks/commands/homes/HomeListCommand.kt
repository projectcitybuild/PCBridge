package com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val remoteConfig: RemoteConfig,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("list")
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1

        val homes = homeRepository.all(
            playerUUID = player.uniqueId,
            page = pageNumber,
            size = remoteConfig.latest.config.warps.itemsPerPage, // TODO: make separate entry?
        )

        if (homes.data.isEmpty()) {
            player.sendRichMessage(
                if (pageNumber == 1) l10n.noHomesFound
                else l10n.errorPageNotFound
            )
            return@scopedSuspending
        }
        val message = PageComponentBuilder().build(
            title = "Your Homes",
            paginated = homes,
            pageCommand = { index -> "/homes list $index" },
            itemClickCommand = { "/home ${it.name}" },
            itemHover = { "Teleport to ${it.name}" },
            itemDecorator = {
                "<aqua>${it.name}</aqua> <gray>(${it.world}: ${it.x.toInt()}, ${it.y.toInt()}, ${it.z.toInt()})</gray>"
            },
        )
        player.sendMessage(message)
    }
}