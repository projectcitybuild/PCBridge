package com.projectcitybuild.modules.moderation.mutes.commands

import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class UnmuteCommand(
    private val mute: MutePlayer,
) {
    fun execute(commandSender: Player, targetPlayer: Player) {
        mute.execute(
            targetPlayer = targetPlayer,
            shouldMute = false,
        )
        commandSender.send().success("${targetPlayer.name} has been unmuted")
        targetPlayer.send().info("You have been unmuted by ${commandSender.name}")
    }
}
