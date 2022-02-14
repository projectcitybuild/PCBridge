package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import javax.inject.Inject

class FlatFileSharedCacheSet @Inject constructor(
    config: PlatformConfig,
    baseFolder: File,
): SharedCacheSet {

    override lateinit var key: String

    private val fileName = key.replace(oldValue = ":", newValue = ".")
    private val fileRelativePath = config.get(PluginConfig.SHARED_CACHE_FILE_RELATIVE_PATH) + "/" + fileName
    private val file = baseFolder.resolve(fileRelativePath)

    @Serializable
    data class Data(val set: Set<String>)

    private fun createFileIfNeeded() {
        if (!file.exists()) {
            file.mkdirs()
            file.createNewFile()
        }
    }

    private fun write(writeOperation: (MutableSet<String>) -> Unit) {
        createFileIfNeeded()

        val data = Json.decodeFromString<Data>(file.readText())
        val mutatedSet = data.set.toMutableSet().also(writeOperation)
        val json = Json.encodeToJsonElement(Data(mutatedSet))

        FileWriter(file, Charset.defaultCharset()).use {
            it.write(json.toString())
        }
    }

    private fun readSet(): Set<String> {
        val fileContents = file.readText()
        val data = Json.decodeFromString<Data>(fileContents)
        return data.set
    }

    override fun has(value: String): Boolean {
        if (!file.exists()) return false

        return readSet().contains(value)
    }

    override fun add(value: String) {
        write { it.add(value) }
    }

    override fun add(values: List<String>) {
        write { it.addAll(values) }
    }

    override fun remove(value: String) {
        write { it.remove(value) }
    }

    override fun removeAll() {
        write { it.clear() }
    }

    override fun all(): Set<String> {
        if (!file.exists()) return emptySet()

        return readSet()
    }
}