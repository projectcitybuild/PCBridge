package com.projectcitybuild.spigot.modules.bans.listeners

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.requests.GameBanStatusRequest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

class BanConnectionListener : Listenable<PlayerJoinEvent> {
    override var environment: Environment? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: PlayerJoinEvent) {
        val environment = environment ?: throw Exception("Environment is null")
        val banApi = environment.apiClient().banApi

        val request = GameBanStatusRequest(playerId = event.player.uniqueId.toString(), playerType = "minecraft_uuid")
        val response = banApi.requestStatus(request).execute()
        val status = response.body()

        if (status != null) {
            environment.log(LogLevel.INFO, status.reason)
        } else {
            environment.log(LogLevel.INFO, "Status is null")
        }
    }
}