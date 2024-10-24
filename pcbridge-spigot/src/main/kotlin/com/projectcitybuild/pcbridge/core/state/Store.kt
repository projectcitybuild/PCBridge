package com.projectcitybuild.pcbridge.core.state

import com.projectcitybuild.pcbridge.core.logger.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val mutex = Mutex()

// TODO: splice the store so that each feature can maintain its own state slice
class Store {
    val state: ServerState
        get() = _state

    private var _state =
        ServerState(
            players = mutableMapOf(),
            lastBroadcastIndex = 0,
        )

    suspend fun mutate(mutation: (ServerState) -> ServerState) =
        withContext(Dispatchers.Default) {
            log.debug { "[previous state]\n$state" }

            mutex.withLock {
                _state = mutation(_state)
                log.debug { "[new state]\n$state" }
            }
        }
}
