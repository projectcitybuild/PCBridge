package com.projectcitybuild.pcbridge.paper.runtime.state.store

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.TracerFactory
import com.projectcitybuild.pcbridge.paper.core.libs.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Persists arbitrary, feature-owned state to storage.
 *
 * Each feature keeps its own state completely separate from other features by
 * reading/mutating it through its own [FeatureStateKey] (see [featureStateKey]).
 * Internally, every feature's state is kept in its own slot, keyed by name, so
 * a feature can neither see nor accidentally clobber another feature's state,
 * and new features can start persisting state without any changes to [Store].
 */
class Store(
    private val file: File,
    private val storage: Storage<Map<String, JsonElement>>,
) {
    private val tracer = TracerFactory.make("store")
    private val mutex = Mutex()
    private val gson = Gson()

    private var slots: Map<String, JsonElement> = emptyMap()

    /**
     * Restores the state from storage
     */
    suspend fun hydrate() =
        tracer.trace("hydrate") {
            log.info { "Hydrating Store state from storage" }

            val deserialized = storage.read(file)
            if (deserialized != null) {
                mutex.withLock { slots = deserialized }
            } else {
                log.info { "No persisted data found" }
            }
        }

    /**
     * Saves the state to storage
     */
    fun persist() =
        tracer.traceSync("persist") {
            logSync.info { "Persisting Store state to storage" }

            storage.writeSync(
                file = file,
                data = slots,
            )
        }

    /**
     * Reads a feature's own slice of state, or its declared default if it
     * hasn't been stored yet.
     */
    fun <T : Any> state(key: FeatureStateKey<T>): T {
        val slot = slots[key.name] ?: return key.default
        return gson.fromJson(slot, key.type)
    }

    /**
     * Mutates a feature's own slice of state in isolation, without affecting
     * any other feature's state, and returns the new value.
     */
    suspend fun <T : Any> mutate(
        key: FeatureStateKey<T>,
        mutation: (T) -> T,
    ): T =
        withContext(Dispatchers.IO) {
            tracer.trace("mutate") {
                mutex.withLock {
                    val prev = state(key)
                    val next = mutation(prev)

                    slots = slots + (key.name to gson.toJsonTree(next))

                    log.debug(
                        "Feature state mutated",
                        mapOf(
                            "key" to key.name,
                            "prev" to prev,
                            "next" to next,
                        ),
                    )

                    next
                }
            }
        }
}
