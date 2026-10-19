package com.projectcitybuild.pcbridge.features.chatbadge.domain

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Badge
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatBadgeFormatter {
    fun format(
        badges: List<Badge>,
        icon: String,
    ): Component? {
        if (badges.isEmpty()) {
            return null
        }
        val formattedBadge =
            Component.text().append(
                l10n.chatBadge.chatBadgeHeader
                    .appendNewline(),
                l10n.chatBadge.chatBadgeDivider.appendNewline(),
            )
        badges.withIndex().forEach { (index, badge) ->
            formattedBadge.append(
                Component.text(badge.unicodeIcon),
                Component.space(),
                Component.text(badge.displayName),
            ).also {
                if (index < badges.size - 1) {
                    it.appendNewline()
                }
            }
        }
        return MiniMessage.miniMessage()
            .deserialize(icon)
            .hoverEvent(HoverEvent.showText(formattedBadge))
    }
}
