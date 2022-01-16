package com.projectcitybuild.modules.config

import com.projectcitybuild.entities.PluginConfig

interface ConfigProvider {

    fun load(vararg keys: PluginConfig.ConfigPath<*>)
    
    fun <T> get(key: PluginConfig.ConfigPath<T>): T
    fun <T> set(key: PluginConfig.ConfigPath<T>, value: T)

    fun get(path: String): Any?
}