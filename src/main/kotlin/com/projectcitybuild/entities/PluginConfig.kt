package com.projectcitybuild.entities

sealed class PluginConfig {

    data class ConfigPath<T>(val key: String, val defaultValue: T)

    companion object Keys {
        private infix fun <T> String.defaultTo(defaultValue: T): ConfigPath<T> {
            return ConfigPath(this, defaultValue)
        }

        val API_KEY = "api.key" defaultTo "FILL_THIS_IN"
        val API_BASE_URL = "api.base_url" defaultTo "https://projectcitybuild.com/api/"
        val API_IS_LOGGING_ENABLED = "api.is_logging_enabled" defaultTo false

        val DB_HOSTNAME = "database.hostname" defaultTo "127.0.0.1"
        val DB_PORT = "database.port" defaultTo 3306
        val DB_NAME = "database.name" defaultTo "pcbridge"
        val DB_USERNAME = "database.username" defaultTo "username"
        val DB_PASSWORD = "database.password" defaultTo "password"

        val WARPS_PER_PAGE = "warps.warps_per_page" defaultTo 15

        val GROUPS_GUEST = "groups.guest" defaultTo "guest"
        val GROUPS_BUILD_PRIORITY = "groups.build_priority" defaultTo arrayListOf(
            "architect",
            "engineer",
            "planner",
            "builder",
            "intern",
        )
        val GROUPS_TRUST_PRIORITY = "groups.trust_priority" defaultTo arrayListOf(
            "admin",
            "sop",
            "op",
            "moderator",
            "trusted+",
            "trusted",
            "member",
        )
        val GROUPS_DONOR_PRIORITY = "groups.donor_priority" defaultTo arrayListOf(
            "donator",
            "legacy-donator",
        )
    }
}