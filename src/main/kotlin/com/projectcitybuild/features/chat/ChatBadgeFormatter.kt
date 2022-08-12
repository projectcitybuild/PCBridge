package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.textcomponentbuilder.add
import com.projectcitybuild.modules.textcomponentbuilder.addIf
import com.projectcitybuild.repositories.ChatBadgeRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.UUID
import javax.inject.Inject

class ChatBadgeFormatter @Inject constructor(
    private val chatBadgeRepository: ChatBadgeRepository,
    private val config: Config,
) {
    fun get(playerUUID: UUID): TextComponent? {
        val badges = chatBadgeRepository.get(playerUUID)
        if (badges.isEmpty()) {
            return null
        }

        val formattedBadges = badges.withIndex().map { (index, badge) ->
            TextComponent()
                .add(badge.unicodeIcon)
                .add(" ") { it.color = ChatColor.WHITE }
                .add(badge.displayName)
                .addIf(index < badges.size - 1, "\n")
                .toLegacyText()
        }

        return TextComponent(
            TextComponent()
                .add(config.get(ConfigKeys.chatBadgeIcon)) {
                    it.hoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text(formattedBadges.joinToString(separator = ""))
                    )
                }
            .add(" ") {
                it.color = ChatColor.RESET
            }
            .toLegacyText()
        )
    }
}
