package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import java.text.SimpleDateFormat
import java.util.*

class CheckBanCommand(
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val banRepository: BanRepository
) : BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"

    override fun validate(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty())
            return CommandResult.INVALID_INPUT

        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
        val targetPlayerName = input.args.first()

        val result = runCatching {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            banRepository.get(targetPlayerUUID = targetPlayerUUID)

        }.onFailure { throwable ->
            input.sender.send().error(
                if (throwable is BanRepository.PlayerAlreadyBannedException)
                    "$targetPlayerName is already banned"
                else
                    throwable.message ?: "An unknown error occurred"
            )
            return
        }

        val ban = result.getOrNull()
        if (ban == null) {
            input.sender.send().info("$targetPlayerName is not currently banned")
        } else {
            val banDate = ban.createdAt.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            }
            val expiryDate = ban.expiresAt?.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            } ?: "Never"

            input.sender.send().info(
                """
                            #${ChatColor.RED}$targetPlayerName is currently banned.
                            #${ChatColor.GRAY}---
                            #${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date » ${ChatColor.WHITE}$banDate
                            #${ChatColor.GRAY}Expires » ${ChatColor.WHITE}$expiryDate
                        """.trimMargin("#"),
                isMultiLine = true
            )
        }
    }
}