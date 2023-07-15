package com.projectcitybuild.modules.moderation.mutes.commands

import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.entity.Player

class UnmuteCommand(
    private val server: Server,
    private val mute: MutePlayer,
) {
    fun execute(commandSender: Player, targetPlayerName: String) {
        val result = mute.execute(
            willBeMuted = false,
            targetPlayerName = targetPlayerName,
            onlinePlayers = server.onlinePlayers.toList(),
        )
        when (result) {
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    MutePlayer.FailureReason.PLAYER_NOT_ONLINE -> "$targetPlayerName is not online"
                }
            )
            is Success -> {
                commandSender.send().success("${result.value.name} has been unmuted")
                result.value.send().info("You have been unmuted by ${commandSender.name}")
            }
        }
    }
}
