package com.projectcitybuild.features.warps.commands.warps

import com.projectcitybuild.data.repositories.WarpRepository
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.support.messages.PaginationBuilder
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

class WarpListCommand(
    private val argsParser: WarpListArgs,
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val itemsPerPage: Int,
) {
    suspend fun onCommand(
        sender: CommandSender,
        args: List<String>,
    ) {
        val validated = argsParser.parse(args)

        val page = warpRepository.all(
            limit = itemsPerPage,
            page = validated.page,
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
                val command = "/warp ${warp.name}"
                val component = Component.text()
                    .content(warp.name)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(command))
                    .hoverEvent(HoverEvent.showText(Component.text(command)))

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
}

class WarpListArgs {
    data class Args(
        val page: Int,
    )

    fun parse(args: List<String>): Args {
        check(args.size <= 1) {
            "Too many arguments given"
        }
        val page = if (args.isEmpty()) 1 else args[0].toIntOrNull()
            ?: error("Page must be a valid number")

        check(page > 0) {
            "Page must be greater than 0"
        }

        return Args(page = page)
    }
}