package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.warps.usecases.warplist.WarpListUseCase
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
import javax.inject.Inject

class WarpsCommand @Inject constructor(
    private val warpListUseCase: WarpListUseCase,
) : SpigotCommand {

    override val label: String = "warps"
    override val permission = "pcbridge.warp.list"
    override val usageHelp = "/warps"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size > 1) {
            throw InvalidCommandArgumentsException()
        }
        val page = try {
            input.args.firstOrNull()?.toInt()
        } catch (e: NumberFormatException) {
            null
        } ?: 1

        val warpList = warpListUseCase.getList(page)
        if (warpList == null) {
            input.sender.send().info("No warps available")
            return
        }

        val clickableWarps = warpList.warps
            .map { name ->
                TextComponent(name).also {
                    it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp $name")
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/warp $name"))
                    it.isUnderlined = true
                }
            }

        val clickableWarpList = TextComponent()
        clickableWarps.forEachIndexed { index, tc ->
            clickableWarpList.add(tc)
            if (index < clickableWarps.size - 1) {
                clickableWarpList.add(", ")
            }
        }

        val tc = TextComponent()
            .add("Warps") { it.isBold = true }
            .addIf(warpList.totalPages > 1, " ($page/${warpList.totalPages})") {
                it.color = ChatColor.GRAY
            }
            .add("\n---\n")
            .add(clickableWarpList)

        input.sender.spigot().sendMessage(tc)
    }
}
