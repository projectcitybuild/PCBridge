package com.projectcitybuild.core.state

import com.projectcitybuild.core.logger.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val mutex = Mutex()

class Store {
    val state: ServerState
        get() = _state

    private var _state = ServerState(
        players = mutableMapOf(),
        lastBroadcastIndex = 0,
    )

    suspend fun mutate(mutation: (ServerState) -> ServerState) = withContext(Dispatchers.Default) {
        logger.debug { "[previous state]\n$state" }

        mutex.withLock {
            _state = mutation(_state)
            logger.debug { "[new state]\n$state" }
        }
    }

    fun persist() {

    }

    fun rehydrate() {
        // TODO: check whether players in the state are still online
    }
}