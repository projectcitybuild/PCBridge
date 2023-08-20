package com.projectcitybuild

import kotlinx.serialization.Serializable

@Serializable
data class ConfigData(
    val api: Api,
    val database: Database,
    val webServer: WebServer,
    val errorReporting: ErrorReporting,
    val localization: Localization,
    val chatBadge: ChatBadge,
    val warps: Warps,
    val integrations: Integrations,
    val announcements: Announcements,
    val groups: Groups,
) {
    companion object {
        val default = ConfigData(
            api = Api(
                token = "FILL_THIS_IN",
                baseUrl = "https://projectcitybuild.com/api/",
                isLoggingEnabled = false,
            ),
            database = Database(
                hostName = "127.0.0.1",
                port = 3306,
                name = "pcbridge",
                username = "FILL_THIS_IN",
                password = "FILL_THIS_IN",
            ),
            webServer = WebServer(
                token = "FILL_THIS_IN",
                port = 8080,
            ),
            errorReporting = ErrorReporting(
                isSentryEnabled = false,
                sentryDsn = "https://<key>@sentry.io/<project>",
            ),
            localization = Localization(
                timeZone = "UTC",
                locale = "en-us",
            ),
            chatBadge = ChatBadge(
                icon = "§6★"
            ),
            warps = Warps(
                itemsPerPage = 15,
            ),
            integrations = Integrations(
                dynmap = Integrations.Dynmap(
                    warpIconName = "portal",
                )
            ),
            announcements = Announcements(
                intervalInMins = 30,
                messages = listOf(
                    "§bJoin the Project City Build Discord server! Type /discord in game!",
                    "§bDonations are the only way to keep PCB running! If you would like to donate to the server, you can do so by typing /donate in game!",
                    "§bVote for us to help keep PCB active! Type /vote in game!",
                    "§bPost screenshots of your builds to the #showcase channel on our Discord to be featured on the PCB Instagram! Type /discord to join!",
                    "§bMake sure to follow the Project City Build Instagram for features of YOUR builds! Type /instagram in game!",
                ),
            ),
            groups = Groups(
                displayPriority = Groups.DisplayPriority(
                    builder = listOf(
                        "architect",
                        "engineer",
                        "planner",
                        "builder",
                        "intern",
                    ),
                    trust = listOf(
                        "developer",
                        "moderator",
                        "trusted+",
                        "trusted",
                        "member",
                    ),
                    donor = listOf(
                        "donator",
                        "legacy-donator",
                    ),
                ),
                donorTierGroupNames = Groups.DonorTierGroupNames(
                    copper = "copper_tier",
                    iron = "iron_tier",
                    diamond = "diamond_tier",
                ),
            ),
        )
    }

    @Serializable
    data class Api(
        val token: String,
        val baseUrl: String,
        val isLoggingEnabled: Boolean
    )

    @Serializable
    data class Database(
        val hostName: String,
        val port: Int,
        val name: String,
        val username: String,
        val password: String,
    )

    @Serializable
    data class WebServer(
        val token: String,
        val port: Int,
    )

    @Serializable
    data class ErrorReporting(
        val isSentryEnabled: Boolean,
        val sentryDsn: String,
    )

    @Serializable
    data class Localization(
        val timeZone: String,
        val locale: String,
    )

    @Serializable
    data class ChatBadge(
        val icon: String,
    )

    @Serializable
    data class Warps(
        val itemsPerPage: Int,
    )

    @Serializable
    data class Integrations(
        val dynmap: Dynmap,
    ) {
        @Serializable
        data class Dynmap(
            val warpIconName: String,
        )
    }

    @Serializable
    data class Announcements(
        val intervalInMins: Int,
        val messages: List<String>,
    )

    @Serializable
    data class Groups(
        val displayPriority: DisplayPriority,
        val donorTierGroupNames: DonorTierGroupNames,
    ) {
        @Serializable
        data class DisplayPriority(
            val builder: List<String>,
            val trust: List<String>,
            val donor: List<String>,
        )

        @Serializable
        data class DonorTierGroupNames(
            val copper: String,
            val iron: String,
            val diamond: String,
        )
    }
}
