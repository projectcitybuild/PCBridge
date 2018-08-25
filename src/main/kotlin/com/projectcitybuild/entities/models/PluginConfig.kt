package com.projectcitybuild.entities.models

open class PluginConfigKey(val key: String)

sealed class PluginConfig {
    sealed class Api {
        class KEY: PluginConfigKey("api.key")
        class BASE_URL: PluginConfigKey("api.base_url")
    }
    sealed class Settings {
        class MAINTENANCE_MODE: PluginConfigKey("settings.maintenance_mode")
    }
}