package com.projectcitybuild

import com.projectcitybuild.pcbridge.core.modules.config.defaultsTo

class ConfigKeys private constructor() {

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

        val announcementMinInterval = "announcements.min_interval" defaultsTo 30
        val announcementMessages = "announcements.messages" defaultsTo listOf(
            "§bJoin the Project City Build Discord server! Type /discord in game!",
            "§bDonations are the only way to keep PCB running! If you would like to donate to the server, you can do so by typing /donate in game!",
            "§bVote for us to help keep PCB active! Type /vote in game!",
            "§bPost screenshots of your builds to the #showcase channel on our Discord to be featured on the PCB Instagram! Type /discord to join!",
            "§bMake sure to follow the Project City Build Instagram for features of YOUR builds! Type /instagram in game!",
        )

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
