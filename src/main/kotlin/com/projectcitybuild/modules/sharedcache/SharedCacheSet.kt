package com.projectcitybuild.modules.sharedcache

/**
 * Represents a cache that is shared between all servers.
 *
 * Anything that conforms to this interface must guarantee that
 * regardless of which server it's used from, the underlying
 * data will be synchronised.
 */
interface SharedCacheSet {
    var key: String

    fun has(value: String): Boolean
    fun add(value: String)
    fun add(values: List<String>)
    fun remove(value: String)
    fun removeAll()
    fun all(): Set<String>
}

