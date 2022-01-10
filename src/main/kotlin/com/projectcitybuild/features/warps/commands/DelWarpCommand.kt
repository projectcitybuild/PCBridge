package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.CommandSender

class DelWarpCommand(
    private val warpFileStorage: WarpFileStorage
): BungeecordCommand {

    override val label: String = "delwarp"
    override val permission = "pcbridge.warp.delete"
    override val usageHelp = "/delwarp <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val warpName = input.args.first()

        if (!warpFileStorage.exists(warpName)) {
            input.sender.send().error("Warp $warpName does not exist")
            return
        }

        // TODO: Add confirmation
        warpFileStorage.delete(warpName)
        input.sender.send().success("Warp $warpName deleted")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpFileStorage.keys()
            else -> null
        }
    }
}
