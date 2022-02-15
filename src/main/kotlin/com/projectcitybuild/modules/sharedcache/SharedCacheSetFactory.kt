package com.projectcitybuild.modules.sharedcache

import javax.inject.Inject

class SharedCacheSetFactory @Inject constructor(
    private val adapter: SharedCacheSet
) {
    fun build(key: String): SharedCacheSet {
        return adapter.also { it.key = "pcbridge:$key" }
    }
}