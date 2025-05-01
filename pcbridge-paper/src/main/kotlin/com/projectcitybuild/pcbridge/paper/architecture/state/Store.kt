package com.projectcitybuild.pcbridge.paper.architecture.state

import com.projectcitybuild.pcbridge.paper.architecture.state.data.ServerState
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.JsonStorage
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val mutex = Mutex()

// TODO: splice the store so that each feature can maintain its own state slice
class Store(
    private val jsonStorage: JsonStorage<ServerState>,
) {
    val state: ServerState
        get() = _state

    private var _state =
        ServerState(
            players = mutableMapOf(),
            lastBroadcastIndex = 0,
            maintenance = false,
        )

    /**
     * Restores the state from storage
     */
    suspend fun hydrate() {
        log.debug { "Hydrating Store state from storage" }

        val deserialized = jsonStorage.read()
        if (deserialized != null) {
            mutate { deserialized }
        } else {
            log.debug { "No persisted data found" }
        }
    }

    /**
     * Saves the state to storage
     */
    fun persist() {
        log.debug { "Persisting Store state to storage" }

        jsonStorage.write(_state)
    }

    suspend fun mutate(mutation: (ServerState) -> ServerState) =
        withContext(Dispatchers.Default) {
            log.debug { "[previous state]\n$state" }

            mutex.withLock {
                _state = mutation(_state)
                log.debug { "[new state]\n$state" }
            }
        }
}