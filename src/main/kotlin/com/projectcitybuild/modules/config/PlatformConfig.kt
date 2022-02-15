package com.projectcitybuild.modules.config

interface PlatformConfig {
    fun <T> get(key: ConfigKey.ConfigPath<T>): T
    fun <T> set(key: ConfigKey.ConfigPath<T>, value: T)

    fun get(path: String): Any?
}