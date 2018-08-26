package com.projectcitybuild.entities.models

open class PluginConfigPair(val key: String, val defaultValue: Any)

sealed class PluginConfig {
    sealed class Api {
        class KEY: PluginConfigPair("api.key", defaultValue = "")
        class BASE_URL: PluginConfigPair("api.base_url", defaultValue = "")
    }
    sealed class Settings {
        class MAINTENANCE_MODE: PluginConfigPair("settings.maintenance_mode", defaultValue = false)
    }
}