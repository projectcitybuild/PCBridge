package com.projectcitybuild.spigot.modules.bans.interactors

import com.projectcitybuild.entities.models.GameBan
import org.bukkit.command.CommandSender

internal class BanStatusCommandInteractor constructor(val sender: CommandSender) {

    fun messageUUIDLookupFailed() {
        sender.sendMessage("Error: Failed to retrieve UUID of given player")
    }

    fun messageBanFailed() {
        sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
    }

    fun messagePlayerNotBanned() {
        sender.sendMessage("Player is not currently banned")
    }

    fun displayBan(playerName: String, ban: GameBan) {
        sender.sendMessage("""
                        $playerName is currently banned.
                        ---
                        Reason: ${ban.reason}
                        Date: ${ban.createdAt}
                        Expires: ${ban.expiresAt ?: "Never"}
                        ---
                    """)
    }

}