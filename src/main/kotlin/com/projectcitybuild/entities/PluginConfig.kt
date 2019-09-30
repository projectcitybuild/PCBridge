package com.projectcitybuild.entities

open class PluginConfigPair(val key: String, val defaultValue: Any)

sealed class PluginConfig {
    sealed class Api {
        class KEY: PluginConfigPair("api.key", defaultValue = "FILL_THIS_IN")
        class BASE_URL: PluginConfigPair("api.base_url", defaultValue = "https://projectcitybuild.com/api/")
    }
    sealed class Settings {
        class MAINTENANCE_MODE: PluginConfigPair("settings.maintenance_mode", defaultValue = false)
    }
}