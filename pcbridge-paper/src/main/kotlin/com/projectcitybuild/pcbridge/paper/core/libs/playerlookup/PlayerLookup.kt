package com.projectcitybuild.pcbridge.paper.core.libs.playerlookup

import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import org.bukkit.Server
import java.util.UUID

class PlayerLookup(
    private val server: Server,
    private val playerDbMinecraftService: PlayerDbMinecraftService,
) {
    suspend fun findUuid(alias: String): UUID? {
        val trimmedAlias = alias.trim()

        val onlinePlayer = server.onlinePlayers.firstOrNull {
            it.name.equals(trimmedAlias, ignoreCase = true)
        }
        if (onlinePlayer != null) {
            return onlinePlayer.uniqueId
        }

        val playerLookup = playerDbMinecraftService.player(trimmedAlias).data
            ?: return null

        val rawUuid = playerLookup.player.id
        return try {
            UUID.fromString(rawUuid)
        } catch (e: Exception) {
            log.error(e, "Could not parse UUID ({uuid}) of fetched player ({player})", rawUuid, trimmedAlias)
            throw IllegalStateException("Invalid Minecraft UUID ($rawUuid)")
        }
    }
}