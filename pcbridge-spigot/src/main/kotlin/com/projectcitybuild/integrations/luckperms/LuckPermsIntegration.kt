package com.projectcitybuild.integrations.luckperms

import com.projectcitybuild.modules.chat.ChatGroupFormatter
import com.projectcitybuild.integrations.SpigotIntegration
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class LuckPermsIntegration(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
    private val chatGroupFormatter: ChatGroupFormatter,
) : Listener, SpigotIntegration {

    private var isEnabled = false
    private val eventSubscriptions: MutableList<EventSubscription<*>> = mutableListOf()

    private lateinit var luckPerms: LuckPerms

    override fun onEnable() {
        try {
            luckPerms = LuckPermsProvider.get()
        } catch (e: Exception) {
            logger.severe("Failed to hook into LuckPerms plugin")
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
