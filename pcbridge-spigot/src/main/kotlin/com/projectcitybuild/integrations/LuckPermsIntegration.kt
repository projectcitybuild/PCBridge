package com.projectcitybuild.integrations

import com.projectcitybuild.core.logger.log
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.Listener

class LuckPermsIntegration(
    // private val plugin: Plugin,
    // private val chatGroupFormatter: ChatGroupFormatter,
) : Listener {
    private var luckPerms: LuckPerms? = null
    // private val eventSubscriptions: MutableList<EventSubscription<*>> = mutableListOf()

    fun enable() {
        try {
            luckPerms = LuckPermsProvider.get()
        } catch (e: Exception) {
            log.error { "Failed to hook into LuckPerms plugin" }
            return
        }
        log.info { "LuckPerms integration enabled" }

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
