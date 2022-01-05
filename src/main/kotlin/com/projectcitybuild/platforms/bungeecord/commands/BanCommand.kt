package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class BanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val banRepository: BanRepository
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        val staffPlayer = if (input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
        if (targetPlayerUUID == null) {
            input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
            return@async
        }

        try {
            banRepository.ban(
                targetPlayerId = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                staffId = staffPlayer?.uniqueId,
                reason = reason
            )
        } catch (throwable: BanRepository.PlayerAlreadyBannedException) {
            input.sender.send().error("$targetPlayerName is already banned")
            return@async
        } catch (throwable: Throwable) {
            input.sender.send().error(throwable.message ?: "An unknown error occurred")
            return@async
        }

        proxyServer.broadcast(
            TextComponent("${ChatColor.GRAY}${input.args.first()} has been banned by ${input.sender.name}: ${reason?.isNotEmpty() ?: "No reason given"}")
        )
        proxyServer.players
            .first { it.name.lowercase() == targetPlayerName.lowercase() || it.uniqueId == targetPlayerUUID }
            ?.disconnect(
                TextComponent("You have been banned").also { it.color = ChatColor.RED }
            )

        return CommandResult.EXECUTED
    }
}