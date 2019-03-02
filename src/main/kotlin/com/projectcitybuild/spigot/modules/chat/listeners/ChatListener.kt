package com.projectcitybuild.spigot.modules.chat.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
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

        val prefix = mutableListOf<String>(chat.getPlayerPrefix(event.player))
        val suffix = mutableListOf<String>(chat.getPlayerSuffix(event.player))

        environment.permissions?.getPlayerGroups(event.player)?.forEach { group ->
            val groupPrefix = chat.getGroupPrefix(event.player.world, group)
            val groupSuffix = chat.getGroupSuffix(event.player.world, group)

            // donators have the [$] appear before everything
            if (group.toLowerCase() == "donator") {
                prefix.add(index = 0, element = groupPrefix)
            } else {
                prefix.add(groupPrefix)
            }
            suffix.add(groupSuffix)
        }

        val name = "${prefix.joinToString(separator = "")} ${event.player.displayName} ${suffix.joinToString(separator = "")}"
        event.format = "<$name> ${event.message}"
    }

}