package com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.config.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.pcb.services.ConfigHttpService
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorReporter
import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import java.io.File

class RemoteConfig(
    private val configHttpService: ConfigHttpService,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val file: File,
    private val storage: Storage<RemoteConfigVersion>,
    private val errorReporter: ErrorReporter,
) {
    private var cached: RemoteConfigVersion? = null

    val latest: RemoteConfigVersion
        get() = cached!!

    suspend fun fetch(): RemoteConfigVersion {
        log.info { "Fetching remote config..." }

        val next = fetchFromHttp()
            ?: fetchFromCache()
            ?: RemoteConfigVersion(-1, RemoteConfigKeyValues())

        set(next, persist = false)
        return next
    }

    suspend fun set(next: RemoteConfigVersion, persist: Boolean = true) {
        val prev = cached
        cached = next

        if (prev != next) {
            log.debug { "Remote config update detected. Broadcasting change..." }

            eventBroadcaster.broadcast(
                RemoteConfigUpdatedEvent(prev, next)
            )
        }
        if (persist) {
            persistToCache(next)
        }
    }

    private suspend fun fetchFromHttp(): RemoteConfigVersion?
        = runCatching { configHttpService.get() }
            .onFailure { e ->
                log.warn { "Failed to fetch remote config. Falling back to last known config..." }
                e.printStackTrace()
                errorReporter.report(e)
            }
            .onSuccess { persistToCache(it) }
            .getOrNull()

    private suspend fun fetchFromCache(): RemoteConfigVersion?
        = storage.read(file).also {
            if (it == null) {
                log.warn { "No cached remote config. Falling back to default config..." }
            }
        }

    private suspend fun persistToCache(config: RemoteConfigVersion)
        = runCatching {
            storage.write(file, config)
        }.onFailure { e ->
            log.error { "Failed to persist remote config" }
            e.printStackTrace()
            errorReporter.report(e)
        }
}