package com.projectcitybuild.pcbridge.core.filestorage

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class JsonFileStorage<T>(
    private val typeToken: TypeToken<T>,
) : FileStorage<T> {
    private val gson =
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
            .create()

    override suspend fun read(file: File): T? =
        withContext(Dispatchers.IO) {
            readSync(file)
        }

    override suspend fun write(
        file: File,
        data: T,
    ) = withContext(Dispatchers.IO) {
        writeSync(file, data)
    }

    override fun readSync(file: File): T? {
        if (!file.exists()) return null
        return gson.fromJson(file.readText(UTF_8), typeToken.type)
    }

    override fun writeSync(
        file: File,
        data: T,
    ) {
        val parent = file.absoluteFile.parentFile
        Files.createDirectories(parent.toPath())
        val temporary = Files.createTempFile(parent.toPath(), "${file.name}.", ".tmp")
        try {
            Files.writeString(temporary, gson.toJson(data, typeToken.type), UTF_8)
            try {
                Files.move(
                    temporary,
                    file.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temporary,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
