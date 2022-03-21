package com.projectcitybuild.features.homes.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.homes.usecases.HomeListUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.platforms.bungeecord.extensions.addIf
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player
import javax.inject.Inject

class HomesCommand @Inject constructor(
    private val homeListUseCase: HomeListUseCase,
): SpigotCommand {

    override val label: String = "homes"
    override val permission = "pcbridge.homes.list"
    override val usageHelp = "/homes"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size > 1) {
            throw InvalidCommandArgumentsException()
        }
        val page = try {
            input.args.firstOrNull()?.toInt()
        } catch (e: NumberFormatException) {
            null
        } ?: 1

        val homeList = homeListUseCase.getList(input.sender.uniqueId, page)
        if (homeList == null) {
            input.sender.send().info("No homes registered")
            return
        }

        val clickableHomes = homeList.homes
            .map { name ->
                TextComponent(name).also {
                    it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home $name")
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/home $name"))
                    it.isUnderlined = true
                }
            }

        val clickableHomeList = TextComponent()
        clickableHomes.forEachIndexed { index, tc ->
            clickableHomeList.add(tc)
            if (index < clickableHomes.size - 1) {
                clickableHomeList.add(", ")
            }
        }

        val tc = TextComponent()
            .add("Homes") { it.isBold = true }
            .addIf(homeList.totalPages > 1, " ($page/${homeList.totalPages})") {
                it.color = ChatColor.GRAY
            }
            .add("\n---\n")
            .add(clickableHomeList)

        input.sender.spigot().sendMessage(tc)
    }
}
