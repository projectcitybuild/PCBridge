package com.projectcitybuild.plugin.integrations.luckperms

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.plugin.integrations.SpigotIntegration
import dagger.Reusable
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.plugin.Plugin
import javax.inject.Inject

@Reusable
class LuckPermsIntegration @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
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
                logger.verbose("Flushing player's chat group cache due to player node mutation: ${event.user.uniqueId}")
                chatGroupFormatter.flush(playerUUID = event.user.uniqueId)
            }
        )
        eventSubscriptions.add(
            luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
                if (event.isGroup) {
                    logger.verbose("Flushing entire chat group cache due to node mutation: ${event.target}")
                    chatGroupFormatter.flushAllCaches()
                }
            }
        )
    }
}
