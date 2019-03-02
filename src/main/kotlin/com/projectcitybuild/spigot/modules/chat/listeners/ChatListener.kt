package com.projectcitybuild.spigot.modules.chat.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listenable<AsyncPlayerChatEvent> {
    override var environment: EnvironmentProvider? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerChatEvent) {
        val environment = environment ?: return

        // player muting
        val sendingPlayer = environment.get(event.player.uniqueId)
        if (sendingPlayer?.isMuted == true) {
            event.isCancelled = true
            event.player.sendMessage("You cannot chat while muted")
            return
        }

        // chat formatting
        val chat = environment.chat
        if (chat == null) {
            throw Exception("Failed to get chat hook")
        }

        var prefix: String? = null
        var suffix: String? = null
        if (sendingPlayer != null) {
            prefix = environment?.get(sendingPlayer.uuid)?.prefix?.replace(oldValue = "&", newValue = "§")
            suffix = environment?.get(sendingPlayer.uuid)?.suffix?.replace(oldValue = "&", newValue = "§")
        }

        val groupNames = mutableListOf<String>()

        environment.permissions?.getPlayerGroups(event.player)?.forEach { group ->
            val groupPrefix = chat.getGroupPrefix(event.player.world, group).replace(oldValue = "&", newValue = "§")
//            val groupSuffix = chat.getGroupSuffix(event.player.world, group).replace(oldValue = "&", newValue = "§")
            val groupName = groupPrefix

            // donators have the [$] appear before everything
            if (group.toLowerCase() == "donator") {
                groupNames.add(index = 0, element = groupName)
            } else {
                groupNames.add(groupName)
            }
        }

        val finalPrefix = prefix ?: ""
        val finalSuffix = suffix ?: ""
        val finalGroups = groupNames.distinct().joinToString(separator = "")
        val name = "$finalPrefix$finalGroups${event.player.displayName}$finalSuffix"

        event.format = "<$name${ChatColor.RESET}> ${event.message}"
    }

}