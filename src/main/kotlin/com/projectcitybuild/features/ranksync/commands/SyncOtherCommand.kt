package com.projectcitybuild.features.ranksync.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.ranksync.SyncPlayerGroupService
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class SyncOtherCommand(
    private val proxyServer: ProxyServer,
    private val syncPlayerGroupService: SyncPlayerGroupService
): BungeecordCommand {

    override val label: String = "syncother"
    override val permission: String = "pcbridge.sync.other"
    override val usageHelp = "/syncother <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val playerName = input.args.first()
        val player = proxyServer.players
            .firstOrNull { it.name.lowercase() == playerName.lowercase() }

        if (player == null) {
            input.sender.send().error("$playerName is not online")
            return
        }

        runCatching {
            syncPlayerGroupService.execute(player.uniqueId)
        }.onFailure { throwable ->
            player.send().error(
                when (throwable) {
                    is SyncPlayerGroupService.AccountNotLinkedException -> "Sync failed: Player does not have a linked PCB account"
                    is SyncPlayerGroupService.PermissionUserNotFoundException -> "Permission user not found. Check that the user exists in the Permission plugin"
                    else -> throwable.message ?: "An unknown error occurred"
                }
            )
            return
        }

        input.sender.send().success("$playerName has been synchronized")
        player.send().success("Your account groups have been synchronized")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            else -> null
        }
    }
}
