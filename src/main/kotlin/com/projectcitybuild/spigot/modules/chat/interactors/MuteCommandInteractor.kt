package com.projectcitybuild.spigot.modules.chat.interactors

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal class MuteCommandInteractor constructor(val sender: CommandSender) {

    fun messagePlayerNotFound(playerName: String) {
        sender.sendMessage("Player $playerName not found")
    }

    fun messagePlayerAlreadyBanned(playerName: String) {
        sender.sendMessage("$playerName is already muted")
    }

    fun notifyOfPlayerMute(targetPlayer: Player) {
        sender.sendMessage("${targetPlayer.name} has been muted")
        targetPlayer.sendMessage("You have been muted by ${sender.name}")
    }

}