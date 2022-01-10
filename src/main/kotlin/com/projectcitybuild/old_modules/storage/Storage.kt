package com.projectcitybuild.old_modules.storage

interface Storage<T> {
    suspend fun load(key: String): T?
    suspend fun save(key: String, value: T)
    suspend fun delete(key: String)
}