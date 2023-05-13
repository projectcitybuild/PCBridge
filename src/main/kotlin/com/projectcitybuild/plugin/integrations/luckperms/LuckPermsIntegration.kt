package com.projectcitybuild.plugin.integrations.luckperms

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.plugin.integrations.SpigotIntegration
import com.projectcitybuild.support.spigot.logger.Logger
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.Plugin

class LuckPermsIntegration(
    private val plugin: Plugin,
    private val logger: Logger,
    private val chatGroupFormatter: ChatGroupFormatter,
) : SpigotListener, SpigotIntegration {

    private var isEnabled = false
    private val eventSubscriptions: MutableList<EventSubscription<*>> = mutableListOf()

    private lateinit var luckPerms: LuckPerms

    override fun onEnable() {
        try {
            luckPerms = LuckPermsProvider.get()
        } catch (e: Exception) {
            logger.fatal("Failed to hook into LuckPerms plugin")
            return
        }
        logger.info("LuckPerms integration enabled")

        isEnabled = true

        listenForCacheInvalidation()
    }

    override fun onDisable() {
        isEnabled = false
    }

    private fun listenForCacheInvalidation() {
        eventSubscriptions.add(
            luckPerms.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
                chatGroupFormatter.flush(playerUUID = event.user.uniqueId)
            }
        )
        eventSubscriptions.add(
            luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
                if (event.isGroup) {
                    chatGroupFormatter.flushAllCaches()
                }
            }
        )
    }
}
