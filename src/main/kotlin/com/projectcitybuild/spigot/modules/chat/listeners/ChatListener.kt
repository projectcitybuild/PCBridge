package com.projectcitybuild.spigot.modules.chat.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.entities.LogLevel
import me.lucko.luckperms.api.Node
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.stream.Collectors

class ChatListener : Listenable<AsyncPlayerChatEvent> {
    override var environment: EnvironmentProvider? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerChatEvent) {
        val environment = environment ?: return
        val permissions = environment.permissions ?: throw Exception("Permission plugin is null")

        // Mute player if necessary
        val sendingPlayer = environment.get(event.player.uniqueId)
        if (sendingPlayer?.isMuted == true) {
            event.isCancelled = true
            event.player.sendMessage("You cannot chat while muted")
            return
        }

        // Format user display name
        val lpUser = permissions.userManager.getUser(event.player.uniqueId)
                ?: throw Exception("Could not load user from LuckPerms")

        val groups = lpUser.ownNodes.stream()
                .filter(Node::isGroupNode)
                .map(Node::getGroupName)
                .collect(Collectors.toSet())

        val prefixes = lpUser.ownNodes.stream()
                .filter(Node::isPrefix)
                .map(Node::getPrefix)
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val suffixes = lpUser.ownNodes.stream()
                .filter(Node::isSuffix)
                .map(Node::getSuffix)
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val groupNames = mutableListOf<String>()
        groups.forEach { group ->
            // Donators have the [$] appear before everything
            if (group.toLowerCase() == "donator") {
                groupNames.add(index = 0, element = group)
            } else {
                groupNames.add(group)
            }
        }

        val name = "$prefixes${ChatColor.RESET} ${event.player.displayName} $suffixes${ChatColor.RESET}"

        event.format = "$groupNames${ChatColor.RESET} <$name}> ${event.message}"
    }

}