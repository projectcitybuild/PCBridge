package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import java.io.File
import javax.inject.Inject

class FlatFileSharedCacheSet @Inject constructor(
    config: PlatformConfig,
    baseFolder: File,
): SharedCacheSet {

    override lateinit var key: String

    private val fileName = key.replace(oldValue = ":", newValue = ".")
    private val fileRelativePath = config.get(PluginConfig.SHARED_CACHE_FILE_RELATIVE_PATH) + "/" + fileName
    private val file = baseFolder.resolve(fileRelativePath)

    private fun createFileIfNeeded() {
        if (!file.exists()) {
            file.mkdirs()
            file.createNewFile()
        }
    }

    override fun has(value: String): Boolean {
        if (!file.exists()) return false

        TODO()
    }

    override fun add(value: String) {
        createFileIfNeeded()

        TODO()
    }

    override fun add(values: List<String>) {
        createFileIfNeeded()

        TODO()
    }

    override fun remove(value: String) {
        createFileIfNeeded()

        TODO()
    }

    override fun removeAll() {
        createFileIfNeeded()

        TODO()
    }

    override fun all(): Set<String> {
        if (!file.exists()) return emptySet()

        TODO()
    }
}