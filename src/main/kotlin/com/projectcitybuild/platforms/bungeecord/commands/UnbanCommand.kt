package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.Success
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.async
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class UnbanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val banRepository: BanRepository
): BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        async {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
            if (targetPlayerUUID == null) {
                input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                return@async
            }

            try {
                banRepository.unban(
                    targetPlayerUUID = targetPlayerUUID,
                    staffId = staffPlayer?.uniqueId
                )
            } catch (throwable: BanRepository.PlayerNotBannedException) {
                input.sender.send().error("$targetPlayerName is not currently banned")
                return@async
            } catch (throwable: Throwable) {
                input.sender.send().error("$targetPlayerName is not currently banned")
                return@async
            }

            proxyServer.broadcast(
                TextComponent("${input.args.first()} has been unbanned").also { it.color = ChatColor.GRAY }
            )
        }

        return CommandResult.EXECUTED
    }
}