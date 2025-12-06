package com.projectcitybuild.pcbridge.paper.core.libs.store

import com.projectcitybuild.pcbridge.paper.architecture.state.data.Session
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val mutex = Mutex()

class SessionStore {
    private val tracer = TracerFactory.make("session")

    val state: Session
        get() = _state

    private var _state = Session()

    suspend fun mutate(
        mutation: (Session) -> Session,
    ) = withContext(Dispatchers.IO) {
        tracer.trace("mutate") {
            val prev = state
            mutex.withLock { _state = mutation(_state) }
            log.debug(
                "Session state mutated", mapOf(
                    "prev" to prev,
                    "next" to state,
                )
            )
        }
    }
}