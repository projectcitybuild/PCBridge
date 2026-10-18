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
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("list")
        .executesSuspending(taskExecutor, ::execute)
        .then(
            Commands
                .argument("page", IntegerArgumentType.integer())
                .executesSuspending(taskExecutor, ::execute),
        ).build()

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
                    l10n.noHomesFound
                } else {
                    l10n.errorPageNotFound
                },
            )
            return@scoped
        }
        val message =
            PageComponentBuilder().build(
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
