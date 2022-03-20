package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.permissions.Permissions
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.connection.ProxiedPlayer
import javax.inject.Inject

class ChatGroupFormatBuilder @Inject constructor(
    private val permissions: Permissions,
    private val config: PlatformConfig
) {
    data class Aggregate(
        val prefix: Array<out BaseComponent>,
        val suffix: Array<out BaseComponent>,
        val groups: TextComponent,
    )

    private val trustedGroups = config.get(ConfigKey.GROUPS_TRUST_PRIORITY)
    private val builderGroups = config.get(ConfigKey.GROUPS_BUILD_PRIORITY)
    private val donorGroups = config.get(ConfigKey.GROUPS_DONOR_PRIORITY)

    fun format(player: ProxiedPlayer): Aggregate {
        val groupNames = permissions.getUserGroups(player.uniqueId)

        var highestTrust: Pair<Int, String>? = null
        var highestBuild: Pair<Int, String>? = null
        var highestDonor: Pair<Int, String>? = null

        for (groupName in groupNames) {
            val lowercaseGroupName = groupName.lowercase()

            val trustIndex = trustedGroups.indexOf(lowercaseGroupName)
            if (trustIndex != -1) {
                if (highestTrust == null || trustIndex < highestTrust.first) {
                    highestTrust = Pair(trustIndex, groupName)
                }
            }

            val builderIndex = builderGroups.indexOf(lowercaseGroupName)
            if (builderIndex != -1) {
                if (highestBuild == null || builderIndex < highestBuild.first) {
                    highestBuild = Pair(builderIndex, groupName)
                }
            }

            val donorIndex = donorGroups.indexOf(lowercaseGroupName)
            if (donorIndex != -1) {
                if (highestDonor == null || donorIndex < highestDonor.first) {
                    highestDonor = Pair(donorIndex, groupName)
                }
            }
        }

        val groupTC = TextComponent()

        if (highestDonor != null) {
            val hoverName = config.get(path = "groups.appearance.${highestDonor.second}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestDonor.second}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = permissions.getGroupDisplayName(highestDonor.second)
            }
            TextComponent
                    .fromLegacyText(displayName)
                    .forEach { c ->
                        if (hoverName != null && hoverName.isNotEmpty()) {
                            c.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverName))
                        }
                        groupTC.addExtra(c)
                    }
        }
        if (highestTrust != null) {
            val hoverName = config.get(path = "groups.appearance.${highestTrust.second}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestTrust.second}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = permissions.getGroupDisplayName(highestTrust.second)
            }
            TextComponent
                .fromLegacyText(displayName)
                .forEach { c ->
                    if (hoverName != null && hoverName.isNotEmpty()) {
                        c.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverName))
                    }
                    groupTC.addExtra(c)
                }
        }
        if (highestBuild != null) {
            val hoverName = config.get(path = "groups.appearance.${highestBuild.second}.hover_name") as? String
            var displayName = config.get(path = "groups.appearance.${highestBuild.second}.display_name") as? String
            if (displayName.isNullOrBlank()) {
                displayName = permissions.getGroupDisplayName(highestBuild.second)
            }
            TextComponent
                .fromLegacyText(displayName)
                .forEach { c ->
                    if (hoverName != null && hoverName.isNotEmpty()) {
                        c.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverName))
                    }
                    groupTC.addExtra(c)
                }
        }

        return Aggregate(
            prefix = TextComponent.fromLegacyText(
                permissions.getUserPrefix(player.uniqueId)
            ),
            suffix = TextComponent.fromLegacyText(
                permissions.getUserSuffix(player.uniqueId)
            ),
            groups = groupTC,
        )
    }
}