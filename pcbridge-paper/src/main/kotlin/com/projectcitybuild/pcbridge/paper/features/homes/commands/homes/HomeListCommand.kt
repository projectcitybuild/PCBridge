package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val remoteConfig: RemoteConfig,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
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
            return@traceSuspending
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