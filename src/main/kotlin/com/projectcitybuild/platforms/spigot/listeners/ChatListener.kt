package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import net.luckperms.api.node.NodeType
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.stream.Collectors

class ChatListener(
        private val environment: EnvironmentProvider
): Listenable<AsyncPlayerChatEvent> {

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerChatEvent) {
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

        val groupNodes = lpUser.nodes.stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .collect(Collectors.toSet())

        val prefixes = lpUser.nodes.stream()
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val suffixes = lpUser.nodes.stream()
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .map { node -> node.metaValue }
                .collect(Collectors.toSet())
                .joinToString(separator = "")

        val groups = mutableListOf<String>()
        groupNodes.forEach { groupNode ->
            val group = permissions.groupManager.getGroup(groupNode.groupName)
            val displayName = group?.displayName ?: groupNode.groupName

            // Donators have the [$] appear before everything
            if (groupNode.groupName.toLowerCase() == "donator") {
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