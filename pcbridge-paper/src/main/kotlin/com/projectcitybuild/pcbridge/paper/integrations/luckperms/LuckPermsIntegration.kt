package com.projectcitybuild.pcbridge.paper.integrations.luckperms

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
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
            deprecatedLog.error(e) { "Failed to hook into LuckPerms plugin" }
            return
        }
        permissions.setProvider(
            LuckPermsPermissionsProvider(instance)
        )
        luckPerms = instance

        deprecatedLog.info { "LuckPerms integration enabled" }
    }

    fun disable() {
        permissions.setProvider(null)
        luckPerms = null
    }
}
