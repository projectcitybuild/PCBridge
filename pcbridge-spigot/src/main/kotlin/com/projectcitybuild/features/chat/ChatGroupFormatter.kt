package com.projectcitybuild.features.chat

import com.projectcitybuild.features.chat.repositories.ChatGroupRepository
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class ChatGroupFormatter(
    private val chatGroupRepository: ChatGroupRepository,
) {
    data class Aggregate(
        val prefix: Component,
        val suffix: Component,
        val groups: Component,
    )

    private val groupPriorities: MutableMap<String, Pair<ChatGroupType, Int>> = mutableMapOf()

    fun format(playerUUID: UUID): Aggregate {
        if (groupPriorities.isEmpty()) {
            buildGroupList()
        }

        val groupNames = chatGroupRepository.getGroupNamesForPlayer(playerUUID)

        var highestTrust: Pair<Int, String>? = null
        var highestBuild: Pair<Int, String>? = null
        var highestDonor: Pair<Int, String>? = null

        for (groupName in groupNames) {
            val lowercaseGroupName = groupName.lowercase()
            val (groupType, priorityIndex) = groupPriorities[lowercaseGroupName]
                ?: continue

            when (groupType) {
                ChatGroupType.TRUST -> if (highestTrust == null || priorityIndex < highestTrust.first) {
                    highestTrust = Pair(priorityIndex, groupName)
                }
                ChatGroupType.BUILD -> if (highestBuild == null || priorityIndex < highestBuild.first) {
                    highestBuild = Pair(priorityIndex, groupName)
                }
                ChatGroupType.DONOR -> if (highestDonor == null || priorityIndex < highestDonor.first) {
                    highestDonor = Pair(priorityIndex, groupName)
                }
            }
        }

        val groupComponent = Component.text()

        if (highestDonor != null) {
            groupComponent.append(groupTextComponent(highestDonor))
        }
        if (highestTrust != null) {
            groupComponent.append(groupTextComponent(highestTrust))
        }
        if (highestBuild != null) {
            groupComponent.append(groupTextComponent(highestBuild))
        }

        return Aggregate(
            prefix = MiniMessage.miniMessage().deserialize(
                chatGroupRepository.getPrefixForPlayer(playerUUID),
            ),
            suffix = MiniMessage.miniMessage().deserialize(
                chatGroupRepository.getSuffixForPlayer(playerUUID),
            ),
            groups = groupComponent.build(),
        )
    }

    private fun groupTextComponent(group: Pair<Int, String>): Component {
        val groupName = group.second
        val hoverName = chatGroupRepository.getGroupHoverName(groupName)
        val displayName = chatGroupRepository.getGroupDisplayName(groupName) ?: groupName

        return MiniMessage.miniMessage()
            .deserialize(displayName)
            .also {
                if (!hoverName.isNullOrEmpty()) {
                    it.hoverEvent(
                        HoverEvent.showText(Component.text(hoverName))
                    )
                }
            }
    }

    private fun buildGroupList() {
        val trustedGroupPriority = chatGroupRepository.getDisplayPriority(ChatGroupType.TRUST)
        val builderGroupPriority = chatGroupRepository.getDisplayPriority(ChatGroupType.BUILD)
        val donorGroupPriority = chatGroupRepository.getDisplayPriority(ChatGroupType.DONOR)

        trustedGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.TRUST, it.index)
        }
        builderGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.BUILD, it.index)
        }
        donorGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.DONOR, it.index)
        }
    }
}
