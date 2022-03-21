package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class FlatFileSharedCacheSet @Inject constructor(
    private val config: PlatformConfig,
    private val baseFolder: File,
) : SharedCacheSet {

    override lateinit var key: String

    private val folder by lazy {
        val relativePath = config.get(ConfigKey.SHARED_CACHE_FILE_RELATIVE_PATH)
        baseFolder.resolve(relativePath)
    }

    private val file by lazy {
        val fileName = key.replace(oldValue = ":", newValue = ".") + ".json"
        File(folder, fileName)
    }

    @Serializable
    data class Data(val set: Set<String>)

    private fun createFileIfNeeded() {
        if (!folder.exists()) {
            folder.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()

            val json = Json.encodeToJsonElement(Data(emptySet()))

            FileWriter(file, Charset.defaultCharset()).use {
                it.write(json.toString())
            }
        }
    }

    private fun write(writeOperation: (MutableSet<String>) -> Unit) {
        createFileIfNeeded()

        val set = readSet()
        val mutatedSet = set.toMutableSet().also(writeOperation)
        val json = Json.encodeToJsonElement(Data(mutatedSet))

        FileOutputStream(file).use { fileOutputStream ->
            fileOutputStream.channel.use { channel ->
                channel.lock().use {
                    val buff = ByteBuffer.wrap(json.toString().toByteArray(StandardCharsets.UTF_8))
                    channel.write(buff)
                }
            }
        }
    }

    private fun readSet(): Set<String> {
        val fileContents = FileInputStream(file).use { fileInputStream ->
            fileInputStream.channel.use { channel ->
                channel.lock(0, Long.MAX_VALUE, true).use {
                    val out = ByteArrayOutputStream()
                    val bufferSize = min(1024, channel.size().toInt())
                    val buff = ByteBuffer.allocate(bufferSize)

                    while (channel.read(buff) > 0) {
                        out.write(buff.array(), 0, buff.position())
                        buff.clear()
                    }
                    String(out.toByteArray(), StandardCharsets.UTF_8)
                }
            }
        }
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
