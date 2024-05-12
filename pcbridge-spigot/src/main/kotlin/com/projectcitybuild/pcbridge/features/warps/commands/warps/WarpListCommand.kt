package com.projectcitybuild.pcbridge.features.warps.commands.warps

import com.projectcitybuild.pcbridge.features.warps.Warp
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.messages.PaginationBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

class WarpListCommand(
    private val warpRepository: WarpRepository,
    private val itemsPerPage: Int,
) : SpigotCommand<WarpListCommand.Args> {
    override val label = "list"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        if (!sender.hasPermission("pcbridge.warp.list")) {
            throw UnauthorizedCommandException()
        }
        val page =
            warpRepository.all(
                limit = itemsPerPage,
                page = args.page,
            )
        if (page.items.isEmpty()) {
            sender.sendMessage(
                Component.text("No warps available")
                    .color(NamedTextColor.GRAY),
            )
            return
        }

        val message =
            PaginationBuilder<Warp>()
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

    data class Args(
        val page: Int,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                val page =
                    if (args.isEmpty()) {
                        1
                    } else {
                        args[0].toIntOrNull()
                            ?: error("Page must be a valid number")
                    }

                check(page > 0) {
                    "Page must be greater than 0"
                }
                return Args(page = page)
            }
        }
    }
}
