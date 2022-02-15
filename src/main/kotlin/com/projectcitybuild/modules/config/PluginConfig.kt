package com.projectcitybuild.modules.config

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

        val REDIS_HOSTNAME = "redis.hostname" defaultTo "localhost"
        val REDIS_PORT = "redis.port" defaultTo 6379
        val REDIS_USERNAME = "redis.username" defaultTo "root"
        val REDIS_PASSWORD = "redis.password" defaultTo ""

        val ERROR_REPORTING_SENTRY_ENABLED = "error_reporting.sentry.enabled" defaultTo false
        val ERROR_REPORTING_SENTRY_DSN = "error_reporting.sentry.dsn" defaultTo "https://<key>@sentry.io/<project>"

        // Supports 'redis' or 'flatfile'
        val SHARED_CACHE_ADAPTER = "shared_cache.adapter" defaultTo "flatfile"
        val SHARED_CACHE_FILE_RELATIVE_PATH = "shared_cache.flatfile.relative_path" defaultTo "../../cache/pcbridge"

        val WARPS_PER_PAGE = "warps.warps_per_page" defaultTo 15

        val TP_REQUEST_AUTO_EXPIRE_SECONDS = "teleports.requests.auto_expiry_in_seconds" defaultTo 20

        val INTEGRATION_DYNMAP_WARP_ICON = "integrations.dynmap.warp_icon" defaultTo "portal"

        val TIME_TIMEZONE = "time.timezone" defaultTo "UTC"
        val TIME_LOCALE = "time.locale" defaultTo "en-us"

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

        val GROUPS_APPEARANCE_ADMIN_DISPLAY_NAME
            = "groups.appearance.admin.display_name" defaultTo "§4[Staff]"
        val GROUPS_APPEARANCE_ADMIN_HOVER_NAME
            = "groups.appearance.admin.hover_name" defaultTo "Administrator"

        val GROUPS_APPEARANCE_SOP_DISPLAY_NAME
            = "groups.appearance.sop.display_name" defaultTo "§c[Staff]"
        val GROUPS_APPEARANCE_SOP_HOVER_NAME
            = "groups.appearance.sop.hover_name" defaultTo "Senior Operator"

        val GROUPS_APPEARANCE_OP_DISPLAY_NAME
            = "groups.appearance.op.display_name" defaultTo "§6[Staff]"
        val GROUPS_APPEARANCE_OP_HOVER_NAME
            = "groups.appearance.op.hover_name" defaultTo "Operator"

        val GROUPS_APPEARANCE_MODERATOR_DISPLAY_NAME
            = "groups.appearance.moderator.display_name" defaultTo "§e[Staff]"
        val GROUPS_APPEARANCE_MODERATOR_HOVER_NAME
            = "groups.appearance.moderator.hover_name" defaultTo "Moderator"

        val GROUPS_APPEARANCE_TRUSTEDPLUS_HOVER_NAME
            = "groups.appearance.trusted+.hover_name" defaultTo "Trusted+"

        val GROUPS_APPEARANCE_TRUSTED_HOVER_NAME
            = "groups.appearance.trusted.hover_name" defaultTo "Trusted"

        val GROUPS_APPEARANCE_DONOR_HOVER_NAME
            = "groups.appearance.donator.hover_name" defaultTo "Donor"

        val GROUPS_APPEARANCE_ARCHITECT_HOVER_NAME
            = "groups.appearance.architect.hover_name" defaultTo "Architect"

        val GROUPS_APPEARANCE_ENGINEER_HOVER_NAME
            = "groups.appearance.engineer.hover_name" defaultTo "Engineer"

        val GROUPS_APPEARANCE_PLANNER_HOVER_NAME
            = "groups.appearance.planner.hover_name" defaultTo "Planner"

        val GROUPS_APPEARANCE_BUILDER_HOVER_NAME
            = "groups.appearance.builder.hover_name" defaultTo "Builder"

        val GROUPS_APPEARANCE_INTERN_HOVER_NAME
            = "groups.appearance.intern.hover_name" defaultTo "Intern"


        // Spigot only

        val SPIGOT_SERVER_NAME = "spigot.server_name" defaultTo "main"
    }
}