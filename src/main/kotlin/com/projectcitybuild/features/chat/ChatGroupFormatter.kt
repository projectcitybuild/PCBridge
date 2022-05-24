package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.permissions.Permissions
import dagger.Reusable
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player
import javax.inject.Inject

@Reusable
class ChatGroupFormatter @Inject constructor(
    private val permissions: Permissions,
    private val config: PlatformConfig
) {
    data class Aggregate(
        val prefix: List<BaseComponent>,
        val suffix: List<BaseComponent>,
        val groups: TextComponent,
    )

    enum class GroupType {
        TRUST,
        BUILD,
        DONOR,
    }

    private val groupPriorities: MutableMap<String, Pair<GroupType, Int>> = mutableMapOf()

    fun format(player: Player): Aggregate {
        if (groupPriorities.isEmpty()) {
            buildGroupList()
        }

        val groupNames = permissions.getUserGroups(player.uniqueId)

        var highestTrust: Pair<Int, String>? = null
        var highestBuild: Pair<Int, String>? = null
        var highestDonor: Pair<Int, String>? = null

        for (groupName in groupNames) {
            val lowercaseGroupName = groupName.lowercase()
            val (groupType, priorityIndex) = groupPriorities[lowercaseGroupName]
                ?: continue

            when (groupType) {
                GroupType.TRUST -> if (highestTrust == null || priorityIndex < highestTrust.first) {
                    highestTrust = Pair(priorityIndex, groupName)
                }
                GroupType.BUILD -> if (highestBuild == null || priorityIndex < highestBuild.first) {
                    highestBuild = Pair(priorityIndex, groupName)
                }
                GroupType.DONOR -> if (highestDonor == null || priorityIndex < highestDonor.first) {
                    highestDonor = Pair(priorityIndex, groupName)
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

        val prefix = TextComponent.fromLegacyText(permissions.getUserPrefix(player.uniqueId)).toList()
        val suffix = TextComponent.fromLegacyText(permissions.getUserSuffix(player.uniqueId)).toList()

        return Aggregate(
            prefix = prefix,
            suffix = suffix,
            groups = groupTC,
        )
    }

    private fun buildGroupList() {
        val trustedGroupPriority = config.get(ConfigKey.GROUPS_TRUST_PRIORITY)
        val builderGroupPriority = config.get(ConfigKey.GROUPS_BUILD_PRIORITY)
        val donorGroupPriority = config.get(ConfigKey.GROUPS_DONOR_PRIORITY)

        trustedGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.TRUST, it.index)
        }
        builderGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.BUILD, it.index)
        }
        donorGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.DONOR, it.index)
        }
    }
}
