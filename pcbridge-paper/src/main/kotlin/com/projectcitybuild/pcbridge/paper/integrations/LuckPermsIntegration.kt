package com.projectcitybuild.pcbridge.paper.integrations

import com.projectcitybuild.pcbridge.paper.core.logger.log
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.Listener

class LuckPermsIntegration : Listener {
    private var luckPerms: LuckPerms? = null

    fun enable() {
        try {
            luckPerms = LuckPermsProvider.get()
        } catch (e: Exception) {
            log.error { "Failed to hook into LuckPerms plugin" }
            return
        }
        log.info { "LuckPerms integration enabled" }
    }

    fun disable() {
        luckPerms = null
    }
}
