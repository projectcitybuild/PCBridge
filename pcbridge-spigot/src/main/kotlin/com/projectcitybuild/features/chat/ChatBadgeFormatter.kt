package com.projectcitybuild.features.chat

import com.projectcitybuild.features.chat.repositories.ChatBadgeRepository
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class ChatBadgeFormatter(
    private val chatBadgeRepository: ChatBadgeRepository,
) {
    fun get(playerUUID: UUID): Component? {
        val badges = chatBadgeRepository.getBadgesForPlayer(playerUUID)
        if (badges.isEmpty()) {
            return null
        }
        val formattedBadges = badges.withIndex().map { (index, badge) ->
            Component.text()
                .append(
                    Component.text(badge.unicodeIcon),
                    Component.space(),
                    Component.text(badge.displayName),
                )
                .also {
                    if (index < badges.size - 1) {
                        it.appendNewline()
                    }
                }
        }
        return MiniMessage.miniMessage().deserialize(chatBadgeRepository.getIcon())
            .hoverEvent(
                HoverEvent.showText(
                    Component.text(formattedBadges.joinToString(separator = ""))
                )
            )
    }
}
