package com.projectcitybuild.pcbridge.features.chat.listeners

import com.projectcitybuild.pcbridge.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.features.chat.repositories.ChatGroupRepository
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class FormatNameChatListener(
    private val chatBadgeRepository: ChatBadgeRepository,
    private val chatGroupRepository: ChatGroupRepository,
) : Listener, ChatRenderer.ViewerUnaware {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncChatEvent) {
        event.renderer(
            ChatRenderer.viewerUnaware(this),
        )
    }

    override fun render(
        source: Player,
        sourceDisplayName: Component,
        message: Component,
    ): Component =
        runBlocking {
            val uuid = source.uniqueId
            val badge = chatBadgeRepository.getComponent(uuid)
            val groups = chatGroupRepository.getAggregate(uuid)

            Component.text()
                .append(
                    badge,
                    groups.prefix,
                    groups.groups,
                    Component.space(),
                    sourceDisplayName,
                    groups.suffix,
                    Component.text(": "),
                    message,
                )
                .build()
        }
}
