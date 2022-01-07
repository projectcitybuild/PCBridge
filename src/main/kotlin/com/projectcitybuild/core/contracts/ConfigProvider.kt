package com.projectcitybuild.core.contracts

import com.projectcitybuild.entities.PluginConfig

interface ConfigProvider {

    fun <T> get(key: PluginConfig.ConfigPath<T>): T
    fun <T> set(key: PluginConfig.ConfigPath<T>, value: T)

    fun get(path: String): Any?
}