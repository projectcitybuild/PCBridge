package com.projectcitybuild.pcbridge.paper.integrations.luckperms

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.Permissions
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.Listener

class LuckPermsIntegration(
    private val permissions: Permissions,
) : Listener {
    private var luckPerms: LuckPerms? = null

    fun enable() {
        val instance: LuckPerms
        try {
            instance = LuckPermsProvider.get()
        } catch (e: Exception) {
            log.error { "Failed to hook into LuckPerms plugin: ${e.message}" }
            return
        }
        permissions.setProvider(
            LuckPermsPermissionsProvider(instance)
        )
        luckPerms = instance

        log.info { "LuckPerms integration enabled" }
    }

    fun disable() {
        permissions.setProvider(null)
        luckPerms = null
    }
}
