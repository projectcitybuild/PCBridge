package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.platforms.bungeecord.extensions.addIf
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class WarpsCommand @Inject constructor(
    private val warpRepository: WarpRepository,
    private val config: PlatformConfig
): BungeecordCommand {

    override val label: String = "warps"
    override val permission = "pcbridge.warp.list"
    override val usageHelp = "/warps"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size > 1) {
            throw InvalidCommandArgumentsException()
        }

        var page = input.args.firstOrNull()?.toInt() ?: 1
        val warpsPerPage = config.get(PluginConfig.WARPS_PER_PAGE)
        val availableWarps = warpRepository.all().map { it.name }
        val totalWarpPages = ceil((availableWarps.size / warpsPerPage).toDouble()).toInt()

        if (availableWarps.isEmpty()) {
            input.sender.send().info("No warps available")
            return
        }

        page = min(page, totalWarpPages)

        val warpList = availableWarps
            .sorted()
            .chunked(warpsPerPage)[max(page - 1, 0)]

        val clickableWarps = warpList.map { name ->
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
            .addIf(totalWarpPages > 1, " ($page/$totalWarpPages)") {
                it.color = ChatColor.GRAY
            }
            .add("\n---\n")
            .add(clickableWarpList)

        input.sender.sendMessage(tc)
    }
}
