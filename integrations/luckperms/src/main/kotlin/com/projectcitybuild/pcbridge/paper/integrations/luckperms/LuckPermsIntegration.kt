package com.projectcitybuild.pcbridge.paper.integrations.luckperms

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.runtime.integrations.PaperIntegration
import com.projectcitybuild.pcbridge.paper.runtime.permissions.Permissions
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.event.Listener

class LuckPermsIntegration(
    private val permissions: Permissions,
) : PaperIntegration,
    Listener {
    override val priority = 300

    private var luckPerms: LuckPerms? = null

    override suspend fun enable() {
        val instance: LuckPerms
        try {
            instance = LuckPermsProvider.get()
        } catch (e: Exception) {
            logSync.error(e) { "Failed to hook into LuckPerms plugin" }
            return
        }
        permissions.setProvider(
            LuckPermsPermissionsProvider(instance),
        )
        luckPerms = instance

        logSync.info { "LuckPerms integration enabled" }
    }

    override fun disable() {
        permissions.setProvider(null)
        luckPerms = null
    }
}
