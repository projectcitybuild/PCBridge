package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.entities.PluginConfig

interface ConfigProvider {

    fun <T> get(key: PluginConfig.Pair<T>): T
    fun <T> set(key: PluginConfig.Pair<T>, value: T)
}