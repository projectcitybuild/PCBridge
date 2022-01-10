package com.projectcitybuild.old_modules.chat

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.permissions.PermissionsGroup
import com.projectcitybuild.modules.permissions.PermissionsManager
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

class ChatGroupFormatBuilder(
    private val permissionsManager: PermissionsManager,
    private val config: ConfigProvider
) {
    data class Aggregate(
        val prefix: Array<out BaseComponent>,
        val suffix: Array<out BaseComponent>,
        val groups: TextComponent,
    )

    private val trustedGroups = config.get(PluginConfig.GROUPS_TRUST_PRIORITY)
    private val builderGroups = config.get(PluginConfig.GROUPS_BUILD_PRIORITY)
    private val donorGroups = config.get(PluginConfig.GROUPS_DONOR_PRIORITY)

    fun format(player: ProxiedPlayer): Aggregate {
        val lpUser = permissionsManager.getUser(player.uniqueId)
                ?: throw Exception("Could not load user from Permission plugin")

        val groupNodes = lpUser.groups()

        var highestTrust: Pair<Int, PermissionsGroup>? = null
        var highestBuild: Pair<Int, PermissionsGroup>? = null
        var highestDonor: Pair<Int, PermissionsGroup>? = null

        for (groupNode in groupNodes) {
            val groupName = groupNode.name.lowercase()

            val trustIndex = trustedGroups.indexOf(groupName)
            if (trustIndex != -1) {
                if (highestTrust == null || trustIndex < highestTrust.first) {
                    highestTrust = Pair(trustIndex, groupNode)
                }
            }

            val builderIndex = builderGroups.indexOf(groupName)
            if (builderIndex != -1) {
                if (highestBuild == null || builderIndex < highestBuild.first) {
                    highestBuild = Pair(builderIndex, groupNode)
                }
            }

            val donorIndex = donorGroups.indexOf(groupName)
            if (donorIndex != -1) {
                if (highestDonor == null || donorIndex < highestDonor.first) {
                    highestDonor = Pair(donorIndex, groupNode)
                }
            }
        }

        val groupTC = TextComponent()

        if (highestDonor != null) {
            val hoverName = config.get(path = "groups.appearance.${highestDonor.second.name}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestDonor.second.name}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = highestDonor.second.getDisplayName(permissionsManager)
            }
            TextComponent
                    .fromLegacyText(displayName)
                    .forEach { c ->
                        if (hoverName.isNullOrBlank()) return@forEach
                        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
                        c.hoverEvent = hoverEvent
                        groupTC.addExtra(c)
                    }
        }
        if (highestTrust != null) {
            val hoverName = config.get(path = "groups.appearance.${highestTrust.second.name}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestTrust.second.name}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = highestTrust.second.getDisplayName(permissionsManager)
            }
            TextComponent
                .fromLegacyText(displayName)
                .forEach { c ->
                    if (hoverName.isNullOrBlank()) return@forEach
                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
                    c.hoverEvent = hoverEvent
                    groupTC.addExtra(c)
                }
        }
        if (highestBuild != null) {
            val hoverName = config.get(path = "groups.appearance.${highestBuild.second.name}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestBuild.second.name}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = highestBuild.second.getDisplayName(permissionsManager)
            }
            TextComponent
                .fromLegacyText(displayName)
                .forEach { c ->
                    if (hoverName.isNullOrBlank()) return@forEach
                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(hoverName).create())
                    c.hoverEvent = hoverEvent
                    groupTC.addExtra(c)
                }
        }

        return Aggregate(
            prefix = TextComponent.fromLegacyText(lpUser.prefixes()),
            suffix = TextComponent.fromLegacyText(lpUser.suffixes()),
            groups = groupTC,
        )
    }
}