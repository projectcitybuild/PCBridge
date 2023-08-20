package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.modules.moderation.bans.actions.CheckUUIDBan
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player

class CheckBanCommand(
    private val checkUUIDBan: CheckUUIDBan,
) {
    suspend fun execute(commandSender: Player, targetPlayerName: String) {
        val result = checkUUIDBan.getBan(targetPlayerName)

        when (result) {
            is Failure -> {
                if (result.reason == CheckUUIDBan.FailureReason.PLAYER_DOES_NOT_EXIST) {
                    commandSender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                }
            }
            is Success -> {
                val ban = result.value
                if (ban == null) {
                    commandSender.send().info("$targetPlayerName is not currently banned")
                } else {
                    commandSender.send().info(
                        """
                            #${ChatColor.RED}$targetPlayerName is currently banned.
                            #${ChatColor.GRAY}---
                            #${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date » ${ChatColor.WHITE}${ban.dateOfBan}
                            #${ChatColor.GRAY}Expires » ${ChatColor.WHITE}${ban.expiryDate}
                        """.trimMargin("#"),
                        isMultiLine = true
                    )
                }
            }
        }
    }
}
