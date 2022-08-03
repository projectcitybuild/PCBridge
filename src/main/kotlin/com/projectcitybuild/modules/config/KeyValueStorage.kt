package com.projectcitybuild.modules.config

import kotlin.reflect.KClass

interface KeyValueStorage {
    fun <T> get(key: ConfigStorageKey<T>): T
    fun <T> set(key: ConfigStorageKey<T>, value: T)

    fun <T: Any> get(type: KClass<out T>, path: String): T?
}