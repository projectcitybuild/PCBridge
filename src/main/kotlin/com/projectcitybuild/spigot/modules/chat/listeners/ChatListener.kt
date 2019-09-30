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

        val lpUser = permissions.userManager.getUser(event.player.uniqueId)
        if (lpUser == null) {
            throw Exception("Could not load user from LuckPerms")
        }

        val groups = lpUser.getAllNodes().stream()
                .filter(Node::isGroupNode)
                .map(Node::getGroupName)
                .collect(Collectors.toSet())

        groups.forEach { group ->
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

        val finalPrefix = if (prefix != null) "$prefix " else ""
        val finalSuffix = if (suffix != null) " $suffix" else ""
        val finalGroups = groupNames.distinct().joinToString(separator = "")

        val name = "$finalPrefix$finalGroups${event.player.displayName}$finalSuffix"

        event.format = "<$name${ChatColor.RESET}> ${event.message}"
    }

}