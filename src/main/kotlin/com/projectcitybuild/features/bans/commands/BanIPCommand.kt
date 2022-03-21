package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.BanIPUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class BanIPCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val banIPUseCase: BanIPUseCase,
) : BungeecordCommand {

    override val label = "banip"
    override val permission = "pcbridge.ban.banip"
    override val usageHelp = "/banip <name|ip> [reason]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val targetIP = proxyServer.players
            .firstOrNull { it.name.lowercase() == input.args.first().lowercase() }
            ?.socketAddress?.toString()
            ?: input.args.first()

        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val bannerName = if (input.isConsoleSender) null else input.sender.name

        val result = banIPUseCase.banIP(targetIP, bannerName, reason)

        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    BanIPUseCase.FailureReason.IP_ALREADY_BANNED -> "$targetIP is already banned"
                    BanIPUseCase.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
        }
        if (result is Success) {
            input.sender.send().success("IP $targetIP has been banned")
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
