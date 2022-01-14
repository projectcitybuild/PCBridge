package com.projectcitybuild.features.ranksync.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.ranksync.SyncPlayerGroupService
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class SyncOtherCommand(
    private val proxyServer: ProxyServer,
    private val syncPlayerGroupService: SyncPlayerGroupService,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label: String = "syncother"
    override val permission: String = "pcbridge.sync.other"
    override val usageHelp = "/syncother <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }

        if (targetPlayer == null) {
            input.sender.send().error("$targetPlayerName is not online")
            return
        }

        runCatching {
            syncPlayerGroupService.execute(targetPlayer.uniqueId)
        }.onFailure { throwable ->
            targetPlayer.send().error(
                when (throwable) {
                    is SyncPlayerGroupService.AccountNotLinkedException -> "Sync failed: Player does not have a linked PCB account"
                    is SyncPlayerGroupService.PermissionUserNotFoundException -> "Permission user not found. Check that the user exists in the Permission plugin"
                    else -> throwable.message ?: "An unknown error occurred"
                }
            )
            return
        }

        input.sender.send().success("$targetPlayerName has been synchronized")
        targetPlayer.send().success("Your account groups have been synchronized")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.startsWith(args.first()) }
            else -> null
        }
    }
}
