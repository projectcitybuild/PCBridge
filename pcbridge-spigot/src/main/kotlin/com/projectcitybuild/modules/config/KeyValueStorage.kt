package com.projectcitybuild.modules.config

interface KeyValueStorage {
    fun <T> get(key: ConfigStorageKey<T>): T
    fun <T> set(key: ConfigStorageKey<T>, value: T)
}
