package com.projectcitybuild.modules.storage

interface Storage {
    fun <T> get(path: StoragePath<T>): T
    fun <T> set(path: StoragePath<T>, value: T)
}
