package com.projectcitybuild.spigot.modules.bans.interactors

import org.bukkit.command.CommandSender

class BanCommandInteractor constructor(
        val sender: CommandSender
    ){

    fun messageUUIDLookupFailed() {
        sender.sendMessage("Error: Failed to retrieve UUID of given player")
    }

    fun messageAlreadyBanned(name: String) {
        sender.sendMessage("$name is already banned")
    }

    fun messageBanFailed() {
        sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
    }

    fun broadcastPlayerBanned(name: String) {
        sender.server.broadcast("$name has been banned", "*")
    }

}