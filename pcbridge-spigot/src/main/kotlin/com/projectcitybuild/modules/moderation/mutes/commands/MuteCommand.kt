package com.projectcitybuild.modules.moderation.mutes.commands

import com.projectcitybuild.modules.moderation.mutes.actions.MutePlayer
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class MuteCommand(
    private val mute: MutePlayer,
) {
    fun execute(commandSender: Player, targetPlayer: Player) {
        mute.execute(
            targetPlayer = targetPlayer,
            shouldMute = true,
        )
        commandSender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.send().info("You have been muted by ${commandSender.name}")
    }
}
