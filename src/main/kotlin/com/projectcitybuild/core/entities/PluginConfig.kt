package com.projectcitybuild.core.entities

sealed class PluginConfig {

    open class Pair<T>(val key: String, val defaultValue: T)

    sealed class API {
        companion object {
            val KEY: Pair<String>
                get() = Pair(
                    key = "api.key",
                    defaultValue = "FILL_THIS_IN"
                )

            val BASE_URL: Pair<String>
                get() = Pair(
                    key = "api.base_url",
                    defaultValue = "https://projectcitybuild.com/api/"
                )

            val IS_LOGGING_ENABLED: Pair<Boolean>
                get() = Pair(
                    key = "api.is_logging_enabled",
                    defaultValue = false
                )
        }
    }

    sealed class SETTINGS {
        companion object {
            val MAINTENANCE_MODE: Pair<Boolean>
                get() = Pair(
                    key = "settings.maintenance_mode",
                    defaultValue = false
                )
        }
    }
}