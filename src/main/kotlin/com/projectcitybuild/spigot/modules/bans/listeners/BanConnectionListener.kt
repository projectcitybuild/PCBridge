package com.projectcitybuild.spigot.modules.bans.listeners

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.contracts.Listenable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class BanConnectionListener : Listenable<PlayerJoinEvent> {
    override var environment: Environment? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: PlayerJoinEvent) {
        val environment = environment ?: throw Exception("Environment is null")
        val banApi = environment.apiClient().banApi

        val request = banApi.requestStatus(playerId = event.player.uniqueId.toString(), playerType = "minecraft_uuid")
        val response = request.execute()

        val status = response.body()?.data ?: return

        if (status.isActive == false) return
        if (status.expiresAt != null && status.expiresAt <= Date().time) return

        event.player.kickPlayer("You are currently banned. Please appeal @ https://projectcitybuild.com")
    }
}