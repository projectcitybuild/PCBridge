package com.projectcitybuild.modules.ranks

import com.projectcitybuild.platforms.spigot.environment.PermissionsGroup
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import com.projectcitybuild.platforms.spigot.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import java.util.*
import kotlin.collections.ArrayList

class ChatGroupBuilder(
    private val permissionsManager: PermissionsManager,
    private val trustedGroups: ArrayList<String>,
    private val builderGroups: ArrayList<String>,
    private val donorGroups: ArrayList<String>,
) {
    init {
        assert(trustedGroups.isNotEmpty())
        assert(builderGroups.isNotEmpty())
        assert(donorGroups.isNotEmpty())
    }

    data class GroupAppearance(
        val displayName: String,
        val hoverName: String
    )

    data class ChatGroups(
        val prefix: String,
        val suffix: String,
        val groups: String
    )

//    fun build(playerUUID: UUID): ChatGroups {
//        val lpUser = permissionsManager.getUser(playerUUID)
//            ?: throw Exception("Could not load user from Permission plugin")
//
//        val groupNodes = lpUser.groups()
//
//        var highestTrust: Pair<Int, PermissionsGroup>? = null
//        var highestBuild: Pair<Int, PermissionsGroup>? = null
//        var highestDonor: Pair<Int, PermissionsGroup>? = null
//
//        for (groupNode in groupNodes) {
//            val groupName = groupNode.name.lowercase()
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
//                .fromLegacyText(displayName)
//                .forEach { c ->
//                    if (hoverName.isNullOrBlank()) return
//                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
//                    c.hoverEvent = hoverEvent
//                    groupTC.addExtra(c)
//                }
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
//        return ChatGroups(
//            prefix = lpUser.prefixes(),
//            suffix = lpUser.suffixes(),
//            groups = groupTC.toString()
//        )
//    }
}