package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.ConfigProvider
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.platforms.bungeecord.extensions.addIf
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class WarpsCommand(
    private val warpFileStorage: WarpFileStorage,
    private val config: ConfigProvider
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
        val availableWarps = warpFileStorage.keys()
        val totalWarpPages = ceil((availableWarps.size / warpsPerPage).toDouble()).toInt()

        page = min(page, totalWarpPages)

        val warpList = availableWarps
            .sortedDescending()
            .chunked(warpsPerPage)[max(page - 1, 0)]

        val clickableWarps = warpList.map { name ->
            TextComponent(name).also {
                it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp $name")
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
            .addIf(totalWarpPages > 1, " (Page $page/$totalWarpPages)") { it.color = ChatColor.GRAY }
            .add("\n---\n")
            .add(clickableWarpList)

        input.sender.sendMessage(tc)
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return null
    }
}
