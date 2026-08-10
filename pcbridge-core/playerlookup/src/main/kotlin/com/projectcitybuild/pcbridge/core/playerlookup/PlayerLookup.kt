package com.projectcitybuild.pcbridge.core.playerlookup

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.shared.support.java.uuidFromUnsanitizedString
import java.util.UUID

class PlayerLookup(
    private val onlinePlayerFinder: OnlinePlayerFinder,
    private val playerDbMinecraftService: PlayerDbMinecraftService,
) {
    suspend fun findUuid(alias: String): UUID? {
        val trimmedAlias = alias.trim()

        val onlinePlayerUuid = onlinePlayerFinder.findByName(trimmedAlias)
        if (onlinePlayerUuid != null) {
            return onlinePlayerUuid
        }

        val playerLookup = playerDbMinecraftService.player(trimmedAlias).data
            ?: return null

        val rawUuid = playerLookup.player.id
        return try {
            uuidFromUnsanitizedString(rawUuid)
        } catch (e: Exception) {
            log.error(e, "Could not parse UUID (${rawUuid}) of fetched player (${playerLookup.player})", rawUuid, trimmedAlias)
            throw IllegalStateException("Invalid Minecraft UUID ($rawUuid)")
        }
    }
}