package com.projectcitybuild.modules.config

data class ConfigStorageKey<T>(
    val path: String,
    val defaultValue: T,
)

infix fun <T> String.defaultsTo(defaultValue: T): ConfigStorageKey<T> {
    return ConfigStorageKey(this, defaultValue)
}
