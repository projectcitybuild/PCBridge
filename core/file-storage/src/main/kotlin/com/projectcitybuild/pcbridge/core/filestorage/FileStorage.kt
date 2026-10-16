package com.projectcitybuild.pcbridge.core.filestorage

import java.io.File

interface FileStorage<T> {
    suspend fun read(file: File): T?

    suspend fun write(
        file: File,
        data: T,
    )

    fun readSync(file: File): T?

    fun writeSync(
        file: File,
        data: T,
    )
}
