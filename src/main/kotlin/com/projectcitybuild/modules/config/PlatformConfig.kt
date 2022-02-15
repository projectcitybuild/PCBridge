package com.projectcitybuild.modules.config

interface PlatformConfig {
    fun <T> get(key: ConfigKeys.ConfigPath<T>): T
    fun <T> set(key: ConfigKeys.ConfigPath<T>, value: T)

    fun get(path: String): Any?
}