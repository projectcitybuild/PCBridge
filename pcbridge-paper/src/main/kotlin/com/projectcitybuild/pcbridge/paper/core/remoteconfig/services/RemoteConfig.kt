package com.projectcitybuild.pcbridge.paper.core.remoteconfig.services

import com.projectcitybuild.pcbridge.paper.core.logger.log
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.http.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.services.ConfigHttpService
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotEventBroadcaster

class RemoteConfig(
    private val configHttpService: ConfigHttpService,
    private val eventBroadcaster: SpigotEventBroadcaster,
) {
    private var cached: RemoteConfigVersion? = null

    val latest: RemoteConfigVersion
        get() = cached!!

    suspend fun fetch(): RemoteConfigVersion {
        log.info { "Fetching remote config..." }

        val next = try {
            configHttpService.get()
        } catch (e: Exception) {
            log.error { "Failed to fetch remote config. Falling back to default config: ${e.message}" }
            e.printStackTrace()
            RemoteConfigVersion(-1, RemoteConfigKeyValues())
        }
        set(next)
        return next
    }

    suspend fun set(next: RemoteConfigVersion) {
        val prev = cached
        cached = next

        if (prev != next) {
            log.debug { "Remote config update detected. Broadcasting change..." }

            eventBroadcaster.broadcast(
                RemoteConfigUpdatedEvent(prev, next)
            )
        }
    }
}