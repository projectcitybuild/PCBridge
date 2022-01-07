package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.ranks.SyncPlayerGroupService
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
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
            input.sender.send().invalidCommandInput(this)
            return
        }

        val playerName = input.args.first()
        val player = proxyServer.players
            .first { it.name.lowercase() == playerName.lowercase() }

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
        }

        input.sender.send().success("$playerName has been synchronized")
        player.send().success("Your account groups have been synchronized")
    }
}
