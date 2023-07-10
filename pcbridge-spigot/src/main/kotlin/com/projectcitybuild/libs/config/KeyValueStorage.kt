package com.projectcitybuild.libs.config

interface KeyValueStorage {
    fun <T> get(key: ConfigStorageKey<T>): T
    fun <T> set(key: ConfigStorageKey<T>, value: T)
}
