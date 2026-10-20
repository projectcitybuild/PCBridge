package com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import org.bukkit.plugin.Plugin

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal() = command("list") {
        argument("page", IntegerArgumentType.integer()) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
        val player = context.source.requirePlayer()
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1

        val homes =
            homeRepository.all(
                playerUUID = player.uniqueId,
                page = pageNumber,
                size = remoteConfig.latest.config.warps.itemsPerPage, // TODO: make separate entry?
            )

        if (homes.data.isEmpty()) {
            player.sendMessage(
                if (pageNumber == 1) {
                    l10n.homes.noHomesFound
                } else {
                    l10n.pagination.errorPageNotFound
                },
            )
            return@scoped
        }
        val message =
            PageComponentBuilder().buildWithComponents(
                title = l10n.homes.homesListTitle,
                paginated = homes,
                pageCommand = { index -> "/homes list $index" },
                itemClickCommand = { "/home ${it.name}" },
                itemHover = { l10n.lists.teleportTo(it.name) },
                itemDecorator = {
                    l10n.homes.homesListItem(
                        it.name,
                        it.world,
                        it.x.toInt(),
                        it.y.toInt(),
                        it.z.toInt(),
                    )
                },
            )
        player.sendMessage(message)
    }
}
