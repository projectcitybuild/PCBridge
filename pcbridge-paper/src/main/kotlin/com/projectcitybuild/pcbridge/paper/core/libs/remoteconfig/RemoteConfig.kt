package com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.http.models.pcb.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.models.pcb.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.services.pcb.ConfigHttpService
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster

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