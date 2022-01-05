package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ProxyServer

class SyncOtherCommand(
    private val proxyServer: ProxyServer,
    private val syncPlayerGroupAction: SyncPlayerGroupAction
): BungeecordCommand {

    override val label: String = "syncother"
    override val permission: String = "pcbridge.sync.other"

    override fun validate(input: BungeecordCommandInput) : CommandResult {
        if (!input.hasArguments || input.args.size > 1) {
            return CommandResult.INVALID_INPUT
        }
        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
        val playerName = input.args.first()
        val player = proxyServer.players
            .first { it.name.lowercase() == playerName.lowercase() }

        if (player == null) {
            input.sender.send().error("$playerName is not online")
            return
        }

        runCatching {
            syncPlayerGroupAction.execute(player.uniqueId)
        }.onFailure { throwable ->
            player.send().error(
                when (throwable) {
                    is SyncPlayerGroupAction.AccountNotLinkedException -> "Sync failed: Player does not have a linked PCB account"
                    is SyncPlayerGroupAction.PermissionUserNotFoundException -> "Permission user not found. Check that the user exists in the Permission plugin"
                    else -> throwable.message ?: "An unknown error occurred"
                }
            )
        }

        input.sender.send().success("$playerName has been synchronized")
        player.send().success("Your account groups have been synchronized")
    }
}
