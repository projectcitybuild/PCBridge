package com.projectcitybuild.core.contracts

import com.projectcitybuild.entities.PluginConfig

interface ConfigProvider {

    fun <T> get(key: PluginConfig.Pair<T>): T
    fun <T> set(key: PluginConfig.Pair<T>, value: T)

    fun get(path: String): Any?
}