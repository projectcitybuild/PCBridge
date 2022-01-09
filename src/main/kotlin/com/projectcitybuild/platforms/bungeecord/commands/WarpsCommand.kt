package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import kotlin.math.ceil
import kotlin.math.max

class WarpsCommand(
    private val warpFileStorage: WarpFileStorage
): BungeecordCommand {

    override val label: String = "warps"
    override val permission = "pcbridge.warp.list"
    override val usageHelp = "/warps"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size > 1) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        var page = input.args.firstOrNull()?.toInt() ?: 1
        val warpsPerPage = 15
        val availableWarps = warpFileStorage.keys()
        val warpPages = ceil((availableWarps.size / warpsPerPage).toDouble()).toInt()

        if (page > warpPages) {
            page = warpPages
        }
        val warpList = availableWarps.chunked(warpsPerPage)[max(page - 1, 0)]

        input.sender.send().info(
            """
            #${ChatColor.BOLD}Warps ${ChatColor.GRAY}(Page $page/$warpPages)${ChatColor.RESET}
            #---
            #${warpList.joinToString(separator = ", ")}
            """.trimMargin("#"), isMultiLine = true
        )
    }
}
