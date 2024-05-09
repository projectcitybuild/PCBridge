package com.projectcitybuild.integrations

import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.support.PlatformLogger
import com.projectcitybuild.support.spigot.SpigotIntegration
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class LuckPermsIntegration(
    // private val plugin: Plugin,
    private val logger: PlatformLogger,
    // private val chatGroupFormatter: ChatGroupFormatter,
) : Listener {
    private var luckPerms: LuckPerms? = null
    // private val eventSubscriptions: MutableList<EventSubscription<*>> = mutableListOf()

    fun enable() {
        try {
            luckPerms = LuckPermsProvider.get()
        } catch (e: Exception) {
            logger.severe("Failed to hook into LuckPerms plugin")
            return
        }
        logger.info("LuckPerms integration enabled")

        // listenForCacheInvalidation()
    }

    fun disable() {
        luckPerms = null
    }

    // TODO
    // private fun listenForCacheInvalidation() {
    //     eventSubscriptions.add(
    //         luckPerms.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
    //             chatGroupFormatter.flush(playerUUID = event.user.uniqueId)
    //         }
    //     )
    //     eventSubscriptions.add(
    //         luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
    //             if (event.isGroup) {
    //                 chatGroupFormatter.flushAllCaches()
    //             }
    //         }
    //     )
    // }
}
