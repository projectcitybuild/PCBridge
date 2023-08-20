package com.projectcitybuild.pcbridge.core.storage

data class StoragePath<T>(
    val key: String,
    val defaultValue: T,
)
