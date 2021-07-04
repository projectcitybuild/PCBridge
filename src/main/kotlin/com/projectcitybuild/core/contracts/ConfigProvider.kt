package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.entities.PluginConfigPair

interface ConfigProvider {

    fun <T> get(key: PluginConfigPair<T>): T
    fun <T> set(key: PluginConfigPair<T>, value: T)
}