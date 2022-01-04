package com.projectcitybuild.platforms.spigot.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin


class ChatListener(
    private val plugin: Plugin
//    private val config: ConfigProvider,
//    private val playerStore: PlayerStore,
//    private val permissionsManager: PermissionsManager,
//    private val logger: LoggerProvider
): Listener {

//    private val trustedGroups = config.get(PluginConfig.GROUPS.TRUST_PRIORITY)
//    private val builderGroups = config.get(PluginConfig.GROUPS.BUILD_PRIORITY)
//    private val donorGroups = config.get(PluginConfig.GROUPS.DONOR_PRIORITY)
//
//    init {
//        if (trustedGroups.isEmpty()) {
//            throw Exception("Trusted group config is empty. Did you forget to set this?")
//        }
//        if (builderGroups.isEmpty()) {
//            throw Exception("Builder group config is empty. Did you forget to set this?")
//        }
//        if (donorGroups.isEmpty()) {
//            throw Exception("Donor group config is empty. Did you forget to set this?")
//        }
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(SubChannel.GLOBAL_CHAT)
        out.writeUTF(event.message)

        event.player.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())

        // Super unsafe, but no other option as cancelling the event (as per the
        // normal way) will interfere with a lot of other plugins
        event.recipients.clear()

//        // Mute player if necessary
//        val sendingPlayer = playerStore.get(event.player.uniqueId)
//        if (sendingPlayer?.isMuted == true) {
//            event.isCancelled = true
//            event.player.sendMessage("You cannot chat while muted")
//            logger.info("${event.player.displayName} tried to talk while muted: ${event.message}")
//            return
//        }
//
//        // Format user display name
//        val lpUser = permissionsManager.getUser(event.player.uniqueId)
//                ?: throw Exception("Could not load user from Permission plugin")
//
//        val groupNodes = lpUser.groups()
//
//        var highestTrust: Pair<Int, PermissionsGroup>? = null
//        var highestBuild: Pair<Int, PermissionsGroup>? = null
//        var highestDonor: Pair<Int, PermissionsGroup>? = null
//
//        for (groupNode in groupNodes) {
//            val groupName = groupNode.name.toLowerCase()
//
//            val trustIndex = trustedGroups.indexOf(groupName)
//            if (trustIndex != -1) {
//                if (highestTrust == null || trustIndex < highestTrust.first) {
//                    highestTrust = Pair(trustIndex, groupNode)
//                }
//            }
//
//            val builderIndex = builderGroups.indexOf(groupName)
//            if (builderIndex != -1) {
//                if (highestBuild == null || builderIndex < highestBuild.first) {
//                    highestBuild = Pair(builderIndex, groupNode)
//                }
//            }
//
//            val donorIndex = donorGroups.indexOf(groupName)
//            if (donorIndex != -1) {
//                if (highestDonor == null || donorIndex < highestDonor.first) {
//                    highestDonor = Pair(donorIndex, groupNode)
//                }
//            }
//        }
//
//        val groupTC = TextComponent()
//
//        if (highestDonor != null) {
//            val hoverName = config.get(path = "groups.appearance.${highestDonor.second.name}.hover_name") as? String
//            var displayName = config.get(path = "groups.appearance.${highestDonor.second.name}.display_name") as? String
//            if (displayName.isNullOrBlank()) {
//                displayName = highestDonor.second.getDisplayName(permissionsManager)
//            }
//            TextComponent
//                    .fromLegacyText(displayName)
//                    .forEach { c ->
//                        if (hoverName.isNullOrBlank()) return
//                        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
//                        c.hoverEvent = hoverEvent
//                        groupTC.addExtra(c)
//                    }
//        }
//        if (highestTrust != null) {
//            val hoverName = config.get(path = "groups.appearance.${highestTrust.second.name}.hover_name") as? String
//            var displayName = config.get(path = "groups.appearance.${highestTrust.second.name}.display_name") as? String
//            if (displayName.isNullOrBlank()) {
//                displayName = highestTrust.second.getDisplayName(permissionsManager)
//            }
//            TextComponent
//                .fromLegacyText(displayName)
//                .forEach { c ->
//                    if (hoverName.isNullOrBlank()) return
//                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
//                    c.hoverEvent = hoverEvent
//                    groupTC.addExtra(c)
//                }
//        }
//        if (highestBuild != null) {
//            val hoverName = config.get(path = "groups.appearance.${highestBuild.second.name}.hover_name") as? String
//            var displayName = config.get(path = "groups.appearance.${highestBuild.second.name}.display_name") as? String
//            if (displayName.isNullOrBlank()) {
//                displayName = highestBuild.second.getDisplayName(permissionsManager)
//            }
//            TextComponent
//                .fromLegacyText(displayName)
//                .forEach { c ->
//                    if (hoverName.isNullOrBlank()) return
//                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
//                    c.hoverEvent = hoverEvent
//                    groupTC.addExtra(c)
//                }
//        }
//
//        val textComponent = TextComponent()
//            .add(TextComponent.fromLegacyText(lpUser.prefixes()))
//            .add(groupTC)
//            .add(" ") { it.color = ChatColor.RESET }
//            .add(TextComponent.fromLegacyText(event.player.displayName))
//            .add(TextComponent.fromLegacyText(lpUser.suffixes()))
//            .add(": ") { it.color = ChatColor.RESET }
//            .add(TextComponent.fromLegacyText(event.message))
//
//        event.isCancelled = true
//
//        event.recipients.forEach { player ->
//            player.spigot().sendMessage(textComponent)
//        }
//
//        // Messages sent to users don't appear in console, so we have to log it manually
//        logger.info("<${event.player.displayName}> ${event.message}")
    }
}