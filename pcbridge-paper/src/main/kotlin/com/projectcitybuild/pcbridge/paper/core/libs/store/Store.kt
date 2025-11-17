package com.projectcitybuild.pcbridge.paper.core.libs.store

import com.projectcitybuild.pcbridge.paper.architecture.state.data.PersistedServerState
import com.projectcitybuild.pcbridge.paper.architecture.state.data.ServerState
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private val mutex = Mutex()

// TODO: splice the store so that each feature can maintain its own state slice
class Store(
    private val file: File,
    private val storage: Storage<PersistedServerState>,
) {
    val state: ServerState
        get() = _state

    private var _state = ServerState()

    /**
     * Restores the state from storage
     */
    suspend fun hydrate() {
        deprecatedLog.info { "Hydrating Store state from storage" }

        val deserialized = storage.read(file)
        if (deserialized != null) {
            mutate { deserialized.toServerState() }
        } else {
            deprecatedLog.info { "No persisted data found" }
        }
    }

    /**
     * Saves the state to storage
     */
    fun persist() {
        deprecatedLog.info { "Persisting Store state to storage" }

        storage.writeSync(
            file = file,
            data = PersistedServerState.fromServerState(_state)
        )
    }

    suspend fun mutate(mutation: (ServerState) -> ServerState) =
        withContext(Dispatchers.IO) {
            deprecatedLog.debug { "[previous state]\n$state" }

            mutex.withLock {
                _state = mutation(_state)
                deprecatedLog.debug { "[new state]\n$state" }
            }
        }
}