package com.projectcitybuild.core.storage

interface Storage {

    fun <T> get(key: StoragePath<T>): T
    fun <T> set(key: StoragePath<T>, value: T)

    @Deprecated("Avoid using if possible")
    fun get(path: String): Any?
}