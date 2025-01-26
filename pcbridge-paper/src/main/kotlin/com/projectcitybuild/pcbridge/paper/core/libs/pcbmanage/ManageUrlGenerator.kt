package com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage

import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import org.bukkit.Server

class ManageUrlGenerator(
    private val server: Server,
    private val localConfig: LocalConfig,
) {
    private val baseUrl: String get() {
        var baseUrl = localConfig.get().api.baseUrl
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        return baseUrl.removeSuffix("api/")
    }

    /**
     * Returns a URL with the given [path] and a UUID URL parameter
     * for the given player. If the player is not online, the UUID
     * will be [playerName]
     */
    fun byPlayerUuid(playerName: String, path: String): String {
        val player = server.onlinePlayers.firstOrNull { it.name == playerName }
        val lookup = player?.uniqueId?.toString() ?: playerName

        return "${baseUrl}${path}?uuid=$lookup"
    }
}