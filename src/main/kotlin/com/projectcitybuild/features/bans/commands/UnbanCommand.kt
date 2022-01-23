package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.bans.usecases.UnbanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class UnbanCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val unbanUseCase: UnbanUseCase,
) : BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"
    override val usageHelp = "/unban <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        val result = unbanUseCase.unban(targetPlayerName, staffPlayer?.uniqueId)
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    UnbanUseCase.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    UnbanUseCase.FailureReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
                }
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}