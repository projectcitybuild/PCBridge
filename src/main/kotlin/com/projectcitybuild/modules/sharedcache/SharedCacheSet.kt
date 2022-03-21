package com.projectcitybuild.modules.sharedcache

/**
 * Represents a Set<String> that is shared between all servers.
 *
 * Ideally being a cache, all operations should be fast.
 *
 * Anything that conforms to this interface must guarantee that regardless of
 * which server it's invoked on, the underlying data will be synchronised and
 * mutated in a thread-safe manner.
 */
interface SharedCacheSet {

    /**
     * The identifier for the set within the cache provider.
     *
     * A cache provider can hold unlimited sets, but each SharedCacheSet
     * instance has access to only 1.
     */
    var key: String

    /**
     * Returns whether the given value exists in the cache set
     *
     * @param value Value to search for
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun has(value: String, subKey: String? = null): Boolean

    /**
     * Inserts the given value into the cache set if it doesn't already exist
     *
     * @param value Value to insert
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun add(value: String, subKey: String? = null)

    /**
     * Inserts the given values into the cache set if they don't already exist
     *
     * @param value Values to insert
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun add(values: List<String>, subKey: String? = null)

    /**
     * Removes the given value from the cache set if it exists
     *
     * @param value Value to remove
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun remove(value: String, subKey: String? = null)

    /**
     * Flushes the entire cache set
     *
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun removeAll(subKey: String? = null)

    /**
     * Returns every value currently in the cache set
     *
     * @param subKey An optional, secondary key that gets appended to `key`
     *               when performing the operation
     */
    fun all(subKey: String? = null): Set<String>
}
