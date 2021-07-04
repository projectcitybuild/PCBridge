package com.projectcitybuild.core.entities

open class PluginConfigPair<T>(val key: String, val defaultValue: T)

sealed class PluginConfig {

    sealed class API {
        class KEY: PluginConfigPair<String>(
                key = "api.key",
                defaultValue = "FILL_THIS_IN"
        )
        class BASE_URL: PluginConfigPair<String>(
                key = "api.base_url",
                defaultValue = "https://projectcitybuild.com/api/"
        )
        class IS_LOGGING_ENABLED: PluginConfigPair<Boolean>(
                key = "api.is_logging_enabled",
                defaultValue = false
        )
    }

    sealed class Settings {
        class MAINTENANCE_MODE: PluginConfigPair<Boolean>(
                key = "settings.maintenance_mode",
                defaultValue = false
        )
    }
}