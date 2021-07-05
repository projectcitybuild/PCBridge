package com.projectcitybuild.core.entities

sealed class PluginConfig {

    open class Pair<T>(val key: String, val defaultValue: T)

    sealed class API {
        class KEY: Pair<String>(
                key = "api.key",
                defaultValue = "FILL_THIS_IN"
        )
        class BASE_URL: Pair<String>(
                key = "api.base_url",
                defaultValue = "https://projectcitybuild.com/api/"
        )
        class IS_LOGGING_ENABLED: Pair<Boolean>(
                key = "api.is_logging_enabled",
                defaultValue = false
        )
    }

    sealed class Settings {
        class MAINTENANCE_MODE: Pair<Boolean>(
                key = "settings.maintenance_mode",
                defaultValue = false
        )
    }
}