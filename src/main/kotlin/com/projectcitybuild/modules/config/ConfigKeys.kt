package com.projectcitybuild.modules.config

interface ConfigKeys {

    val API_ENABLED: Boolean
        get() = false

    val API_KEY: String
        get() = "FILL_THIS_IN"

    val API_TOKEN: String
        get() = "FILL_THIS_IN"

    val API_BASE_URL: String
        get() = "https://projectcitybuild.com/api/"

    val API_IS_LOGGING_ENABLED: Boolean
        get() = false


    val DB_HOSTNAME: String
        get() = "127.0.0.1"

    val DB_PORT: Int
        get() = 3306

    val DB_NAME: String
        get() = "pcbridge"

    val DB_USERNAME: String
        get() = "username"

    val DB_PASSWORD: String
        get() = "password"


    val REDIS_HOSTNAME: String
        get() = "localhost"

    val REDIS_PORT: Int
        get() = 6379

    val REDIS_USERNAME: String
        get() = "root"

    val REDIS_PASSWORD: String
        get() = ""


    val ERROR_REPORTING_SENTRY_ENABLED: Boolean
        get() = false

    val ERROR_REPORTING_SENTRY_DSN: String
        get() = "https://<key>@sentry.io/<project>"


    // Supports 'redis' or 'flatfile'
    val SHARED_CACHE_ADAPTER: String
        get() = "flatfile"

    val SHARED_CACHE_FILE_RELATIVE_PATH: String
        get() = "../../cache/pcbridge"


    val TIME_TIMEZONE: String
        get() = "UTC"

    val TIME_LOCALE: String
        get() = "en-us"


    val WARPS_PER_PAGE: Int
        get() = 15


    val INTEGRATION_DYNMAP_WARP_ICON: String
        get() = "portal"


    val GROUPS_GUEST: String
        get() = "guest"

    val GROUPS_BUILD_PRIORITY: List<String>
        get() = listOf(
            "architect",
            "engineer",
            "planner",
            "builder",
            "intern",
        )

    val GROUPS_TRUST_PRIORITY: List<String>
        get() = listOf(
            "developer",
            "moderator",
            "trusted+",
            "trusted",
            "member",
        )

    val GROUPS_DONOR_PRIORITY: List<String>
        get() = listOf(
            "donator",
            "legacy-donator",
        )

    @Deprecated("Not needed anymore")
    val SPIGOT_SERVER_NAME
        get() = "main"
}