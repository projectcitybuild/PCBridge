package com.projectcitybuild.pcbridge.paper.runtime.state.store

/**
 * A typed identifier for a feature's own slice of state within [Store].
 *
 * Each feature defines its own [FeatureStateKey] (backed by its own data class)
 * to read/mutate its slice of state. Because a feature can only look up its
 * state by its own key and type, one feature's state can never leak into, or
 * be overwritten by, another feature's state - and new features can start
 * persisting state without needing to modify [Store] or any other feature.
 */
data class FeatureStateKey<T : Any>(
    val name: String,
    val type: Class<T>,
    val default: T,
)

/**
 * Creates a [FeatureStateKey] for feature state of type [T].
 *
 * [name] must be unique across all features, as it's used as the storage key.
 */
inline fun <reified T : Any> featureStateKey(
    name: String,
    default: T,
): FeatureStateKey<T> = FeatureStateKey(
    name = name,
    type = T::class.java,
    default = default,
)
