package com.projectcitybuild.core.storage

data class StoragePath<T>(val key: String, val defaultValue: T)

infix fun <T> String.defaultsTo(defaultValue: T): StoragePath<T> {
    return StoragePath(this, defaultValue)
}