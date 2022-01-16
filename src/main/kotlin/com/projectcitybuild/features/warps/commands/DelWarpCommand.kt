package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import javax.inject.Inject

class DelWarpCommand @Inject constructor(
    private val warpRepository: WarpRepository
): BungeecordCommand {

    override val label: String = "delwarp"
    override val permission = "pcbridge.warp.delete"
    override val usageHelp = "/delwarp <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val warpName = input.args.first()

        if (!warpRepository.exists(warpName)) {
            input.sender.send().error("Warp $warpName does not exist")
            return
        }

        // TODO: Add confirmation
        warpRepository.delete(warpName)
        input.sender.send().success("Warp $warpName deleted")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpRepository.all().map { it.name }
            else -> null
        }
    }
}
