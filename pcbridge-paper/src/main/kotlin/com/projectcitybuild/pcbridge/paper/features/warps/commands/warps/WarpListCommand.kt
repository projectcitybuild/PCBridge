package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.SimplePaginator
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import com.projectcitybuild.pcbridge.paper.core.support.messages.PaginationBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val pageNumber = runCatching {
            context.getArgument("page", Int::class.java)
        }.getOrElse { 1 }

        val sender = context.source.sender
        val itemsPerPage = remoteConfig.latest.config.warps.itemsPerPage
        val warps = warpRepository.all()
        val page = SimplePaginator<Warp>().paginate(
            items = warps,
            pageSize = itemsPerPage,
            page = pageNumber,
        )
        if (page.items.isEmpty()) {
            sender.sendMessage(
                Component.text("No warps available")
                    .color(NamedTextColor.GRAY),
            )
            return@traceCommand
        }

        val message = PaginationBuilder<Warp>()
            .items { (index, warp) ->
                val rawCommand = "/warp ${warp.name}"
                val component =
                    Component.text()
                        .content(warp.name)
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.runCommand(rawCommand))
                        .hoverEvent(HoverEvent.showText(Component.text(rawCommand)))

                if (index < page.items.size - 1) {
                    component.append(
                        Component.text(" / ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.UNDERLINED, false),
                    )
                }
                component.build()
            }
            .build(page)

        sender.sendMessage(message)
    }
}
