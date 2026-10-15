package com.projectcitybuild.pcbridge.runtime.playerlookup

import com.projectcitybuild.pcbridge.core.support.java.uuidFromUnsanitizedString
import com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import org.bukkit.Server
import java.util.UUID

class PlayerLookup(
    private val server: Server,
    private val playerDbMinecraftService: PlayerDbMinecraftService,
) {
    suspend fun findUuid(alias: String): UUID? {
        val trimmedAlias = alias.trim()

        val onlinePlayer = server.onlinePlayer(name = trimmedAlias)
        if (onlinePlayer != null) {
            return onlinePlayer.uniqueId
        }

        val playerLookup =
            playerDbMinecraftService.player(trimmedAlias).data
                ?: return null

        val rawUuid = playerLookup.player.id
        return try {
            uuidFromUnsanitizedString(rawUuid)
        } catch (e: Exception) {
            log.error(e, "Could not parse UUID ({uuid}) of fetched player ({player})", rawUuid, trimmedAlias)
            throw IllegalStateException("Invalid Minecraft UUID ($rawUuid)")
        }
    }
}
