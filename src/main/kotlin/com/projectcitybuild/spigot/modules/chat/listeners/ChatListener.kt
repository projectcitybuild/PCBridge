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

//        val prefix = mutableListOf<String>(chat.getPlayerPrefix(event.player)).distinct().map { suffix -> suffix.replace(oldValue = "&", newValue = "§") }
//        val suffix = mutableListOf<String>(chat.getPlayerSuffix(event.player)).distinct().map { suffix -> suffix.replace(oldValue = "&", newValue = "§") }
        val prefix = mutableListOf<String>()
        val suffix = mutableListOf<String>()

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

        val finalPrefix = prefix.distinct().joinToString(separator = "")
        val finalSuffix = suffix.distinct().joinToString(separator = "")
        val finalGroups = groupNames.distinct().joinToString(separator = "")

        val name = "$finalPrefix$finalGroups${event.player.displayName}$finalSuffix"

        event.format = "<$name${ChatColor.RESET}> ${event.message}"
    }

}