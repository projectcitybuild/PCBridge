package com.projectcitybuild.spigot.modules.chat.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
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

        val groupNodes = lpUser.ownNodes.stream()
                .filter(Node::isGroupNode)
                .map { node -> permissions.groupManager.getGroup(node.groupName) }
                .collect(Collectors.toSet())

        val prefixes = lpUser.ownNodes.stream()
                .filter(Node::isPrefix)
                .map { node -> node.prefix.value }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val suffixes = lpUser.ownNodes.stream()
                .filter(Node::isSuffix)
                .map { node -> node.suffix.value }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val groups = mutableListOf<String>()
        groupNodes.forEach { group ->
            val group = group ?: return
            val displayName = group.displayName ?: group.name

            // Donators have the [$] appear before everything
            if (group.name.toLowerCase() == "donator") {
                groups.add(index = 0, element = displayName)
            } else {
                groups.add(displayName)
            }
        }
        val groupNames = groups.joinToString(separator = "")

        val name = "$prefixes${ChatColor.RESET} ${event.player.displayName} $suffixes${ChatColor.RESET}"
        val message = "$groupNames${ChatColor.RESET} <$name> ${event.message}"

        // Escape % from message
        val escapedMessage = message.replace("%", newValue = "%%")

        event.format = escapedMessage
    }

}