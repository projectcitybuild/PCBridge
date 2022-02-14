package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import javax.inject.Inject

class FlatFileSharedCacheSet @Inject constructor(): SharedCacheSet {

    override lateinit var key: String

    private val fileName = key.replace(oldValue = ":", newValue = ".")

    override fun has(value: String): Boolean {
        TODO()
    }

    override fun add(value: String) {
        TODO()
    }

    override fun add(values: List<String>) {
        TODO()
    }

    override fun remove(value: String) {
        TODO()
    }

    override fun removeAll() {
        TODO()
    }

    override fun all(): Set<String> {
        TODO()
    }
}