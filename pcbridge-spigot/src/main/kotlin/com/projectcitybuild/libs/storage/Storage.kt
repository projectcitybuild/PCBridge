package com.projectcitybuild.libs.storage

interface Storage {
    fun <T> get(path: StoragePath<T>): T
    fun <T> set(path: StoragePath<T>, value: T)
}
