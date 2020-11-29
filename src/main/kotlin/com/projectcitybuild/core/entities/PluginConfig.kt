package com.projectcitybuild.core.entities

open class PluginConfigPair(val key: String, val defaultValue: Any)

sealed class PluginConfig {
    sealed class API {
        class KEY: PluginConfigPair(key = "api.key", defaultValue = "FILL_THIS_IN")
        class BASE_URL: PluginConfigPair("api.base_url", defaultValue = "https://projectcitybuild.com/api/")
        class IS_LOGGING_ENABLED: PluginConfigPair("api.is_logging_enabled", defaultValue = false)
    }
    sealed class Settings {
        class MAINTENANCE_MODE: PluginConfigPair("settings.maintenance_mode", defaultValue = false)
    }
}