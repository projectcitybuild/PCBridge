package com.projectcitybuild.libs.config

class ConfigKeys {
    companion object {
        val apiToken = "api.token" defaultsTo "FILL_THIS_IN"
        val apiBaseURL = "api.base_url" defaultsTo "https://projectcitybuild.com/api/"
        val apiIsLoggingEnabled = "api.is_logging_enabled" defaultsTo false

        val dbHostName = "database.hostname" defaultsTo "127.0.0.1"
        val dbPort = "database.port" defaultsTo 3306
        val dbName = "database.name" defaultsTo "pcbridge"
        val dbUsername = "database.username" defaultsTo "FILL_THIS_IN"
        val dbPassword = "database.password" defaultsTo "FILL_THIS_IN"

        val internalWebServerToken = "webserver.bearer" defaultsTo "FILL_THIS_IN"
        val internalWebServerPort = "webserver.port" defaultsTo 8080

        val errorReportingSentryEnabled = "error_reporting.sentry.enabled" defaultsTo false
        val errorReportingSentryDSN = "error_reporting.sentry.dsn" defaultsTo "https://<key>@sentry.io/<project>"

        val timeTimezone = "time.timezone" defaultsTo "UTC"
        val timeLocale = "time.locale" defaultsTo "en-us"

        val chatBadgeIcon = "chat.badge.icon" defaultsTo "§6★"

        val warpsPerPage = "warps.warps_per_page" defaultsTo 15

        val integrationDynmapWarpIcon = "integrations.dynmap.warp_icon" defaultsTo "portal"

        val groupsBuildPriority = "groups.build_priority" defaultsTo listOf(
            "architect",
            "engineer",
            "planner",
            "builder",
            "intern",
        )
        val groupsTrustPriority = "groups.trust_priority" defaultsTo listOf(
            "developer",
            "moderator",
            "trusted+",
            "trusted",
            "member",
        )
        val groupsDonorPriority = "groups.donor_priority" defaultsTo listOf(
            "donator",
            "legacy-donator",
        )
    }
}
