package com.projectcitybuild.pcbridge.core.storage

import java.io.File

interface Storage<T> {
    suspend fun read(file: File): T?

    suspend fun write(file: File, data: T)

    fun readSync(file: File): T?

    fun writeSync(file: File, data: T)
}