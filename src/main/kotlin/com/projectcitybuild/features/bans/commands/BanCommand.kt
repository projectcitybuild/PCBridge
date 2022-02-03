package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.bans.usecases.BanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class BanCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val banUseCase: BanUseCase,
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"
    override val usageHelp = "/ban <name> [reason]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val staffPlayer = if (input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        val result = banUseCase.ban(
            targetPlayerName,
            bannerUUID = staffPlayer?.uniqueId,
            bannerName = input.sender.name ?: "CONSOLE",
            reason
        )
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    BanUseCase.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    BanUseCase.FailureReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
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