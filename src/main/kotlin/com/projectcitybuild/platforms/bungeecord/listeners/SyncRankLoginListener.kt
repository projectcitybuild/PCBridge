package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordLogger
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordScheduler
import com.projectcitybuild.platforms.bungeecord.permissions.PermissionsManager
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class SyncRankLoginListener(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val scheduler: BungeecordScheduler,
    private val permissionsManager: PermissionsManager,
    private val logger: BungeecordLogger
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PostLoginEvent) {

    }
}