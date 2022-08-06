package com.projectcitybuild.plugin.commands

import com.projectcitybuild.features.warps.usecases.GetWarpListUseCase
import com.projectcitybuild.modules.textcomponentbuilder.add
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.support.annotations.annotations.Command
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import javax.inject.Inject

@Command(
    name = "warps",
    desc = "Gets a list of all warps available",
    usage = "/warps",
)
class WarpsCommand @Inject constructor(
    private val getWarpListUseCase: GetWarpListUseCase,
) : SpigotCommand {

    override val label = "warps"
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

        val warpList = getWarpListUseCase.getList(page)
        if (warpList == null) {
            input.sender.send().info("No warps available")
            return
        }

        val tc = TextComponent()
            .add("Warps (${warpList.totalWarps})") { it.isBold = true }
            .add("\n---\n")

        warpList.warps.withIndex().forEach { (index, name) ->
            if (index != 0) {
                tc.add(", ")
            }
            tc.add(
                TextComponent(name).also {
                    it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp $name")
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/warp $name"))
                    it.isUnderlined = true
                }
            )
        }

        if (warpList.totalPages > 1) {
            tc.add("\n---\n")
                .add("Page $page of ${warpList.totalPages}") { it.color = ChatColor.GRAY }
        }

        input.sender.spigot().sendMessage(tc)
    }
}
