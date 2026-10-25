package com.projectcitybuild.pcbridge.runtime.remoteconfig

import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.ConfigHttpService
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import kotlinx.coroutines.CancellationException
import java.io.File

class RemoteConfig(
    private val configHttpService: ConfigHttpService,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val file: File,
    private val fileStorage: FileStorage<RemoteConfigVersion>,
    private val errorTracker: ErrorTracker,
) {
    private var cached: RemoteConfigVersion? = null

    val latest: RemoteConfigVersion
        get() = checkNotNull(cached) { "Remote configuration has not been initialized" }

    /**
     * Loads the first configuration snapshot without broadcasting an update event.
     */
    suspend fun initialize(): RemoteConfigVersion {
        cached?.let { return it }
        val initial = load()
        cached = initial
        return initial
    }

    /**
     * Fetches and publishes configuration changes after startup listeners are active.
     */
    suspend fun fetch(): RemoteConfigVersion {
        val next = load()
        set(next, persist = false)
        return next
    }

    suspend fun set(
        next: RemoteConfigVersion,
        persist: Boolean = true,
    ) {
        val prev = cached
        cached = next

        if (prev != next) {
            log.debug { "Remote config update detected. Broadcasting change..." }
            eventBroadcaster.broadcast(RemoteConfigUpdatedEvent(prev, next))
        }
        if (persist) {
            persistToCache(next)
        }
    }

    private suspend fun load(): RemoteConfigVersion {
        log.info { "Fetching remote config..." }
        return fetchFromHttp()
            ?: fetchFromCache()
            ?: RemoteConfigVersion(-1, RemoteConfigKeyValues())
    }

    private suspend fun fetchFromHttp(): RemoteConfigVersion? =
        try {
            configHttpService.get().also { persistToCache(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn { "Failed to fetch remote config. Falling back to last known config..." }
            errorTracker.reportSafely(e)
            null
        }

    private suspend fun fetchFromCache(): RemoteConfigVersion? =
        fileStorage.read(file).also {
            if (it == null) {
                log.warn { "No cached remote config. Falling back to default config..." }
            }
        }

    private suspend fun persistToCache(config: RemoteConfigVersion) {
        try {
            fileStorage.write(file, config)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.error(e) { "Failed to persist remote config" }
            errorTracker.reportSafely(e)
        }
    }
}
