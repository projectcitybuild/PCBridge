package com.projectcitybuild.pcbridge.core.remoteconfig

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.storage.Storage
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.pcb.services.ConfigHttpService
import java.io.File

class RemoteConfig(
    private val configHttpService: ConfigHttpService,
    private val onEventFired: suspend (RemoteConfigUpdatedEvent) -> Unit,
    private val file: File,
    private val storage: Storage<RemoteConfigVersion>,
    private val errorTracker: ErrorTracker,
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

            onEventFired(
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
                errorTracker.report(e)
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
            log.error(e) { "Failed to persist remote config" }
            e.printStackTrace()
            errorTracker.report(e)
        }
}