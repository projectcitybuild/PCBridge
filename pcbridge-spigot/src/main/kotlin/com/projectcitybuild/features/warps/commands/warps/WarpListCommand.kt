package com.projectcitybuild.features.warps.commands.warps

import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.messages.PaginationBuilder
import com.projectcitybuild.support.spigot.ArgsParser
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpListCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val itemsPerPage: Int,
): SpigotCommand<WarpListCommand.Args> {
    override val usage: CommandHelpBuilder
        get() = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, command: Command, args: Args) {
        val page = warpRepository.all(
            limit = itemsPerPage,
            page = args.page,
        )
        if (page.items.isEmpty()) {
            audiences.sender(sender).sendMessage(
                Component.text("No warps available")
                    .color(NamedTextColor.GRAY)
            )
            return
        }

        val message = PaginationBuilder<Warp>()
            .items { (index, warp) ->
                val rawCommand = "/warp ${warp.name}"
                val component = Component.text()
                    .content(warp.name)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(rawCommand))
                    .hoverEvent(HoverEvent.showText(Component.text(rawCommand)))

                if (index < page.items.size - 1) {
                    component.append(
                        Component.text(" / ")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.UNDERLINED, false)
                    )
                }
                component.build()
            }
            .build(page)

        audiences.sender(sender).sendMessage(message)
    }

    data class Args(
        val page: Int,
    ) {
        class Parser: ArgsParser<Args> {
            override fun tryParse(args: List<String>): Args {
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                val page = if (args.isEmpty()) 1 else args[0].toIntOrNull()
                    ?: error("Page must be a valid number")

                check (page > 0) {
                    "Page must be greater than 0"
                }

                return Args(page = page)
            }
        }
    }
}