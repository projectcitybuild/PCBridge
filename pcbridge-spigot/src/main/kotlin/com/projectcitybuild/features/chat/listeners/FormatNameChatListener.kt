package com.projectcitybuild.features.chat.listeners

import com.projectcitybuild.features.chat.ChatBadgeFormatter
import com.projectcitybuild.features.chat.ChatGroupFormatter
import io.github.reactivecircus.cache4k.Cache
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.UUID

class FormatNameChatListener(
    private val chatGroupFormatter: ChatGroupFormatter,
    private val chatBadgeFormatter: ChatBadgeFormatter,
    private val badgeCache: Cache<UUID, Component>,
    private val groupCache: Cache<UUID, ChatGroupFormatter.Aggregate>,
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
    ): Component = runBlocking {
        val uuid = source.uniqueId
        val badge = badgeCache.get(uuid) {
            chatBadgeFormatter.get(uuid) ?: Component.empty()
        }
        val groups = groupCache.get(uuid) {
            chatGroupFormatter.format(uuid)
        }
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
