@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.data

import kotlinx.serialization.Serializable

@Serializable
data class PluginConfig(
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
    val messages: Messages,
) {
    @Serializable
    data class Api(
        val token: String,
        val baseUrl: String,
        val isLoggingEnabled: Boolean,
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

    @Serializable
    data class Messages(
        val join: String,
        val leave: String,
        val firstTimeJoin: String,
        val welcome: String,
    )

    companion object
}

fun PluginConfig.Companion.default() =
    PluginConfig(
        api =
            PluginConfig.Api(
                token = "FILL_THIS_IN",
                baseUrl = "https://projectcitybuild.com/api/",
                isLoggingEnabled = false,
            ),
        database =
            PluginConfig.Database(
                hostName = "127.0.0.1",
                port = 3306,
                name = "pcbridge",
                username = "FILL_THIS_IN",
                password = "FILL_THIS_IN",
            ),
        webServer =
            PluginConfig.WebServer(
                token = "FILL_THIS_IN",
                port = 8080,
            ),
        errorReporting =
            PluginConfig.ErrorReporting(
                isSentryEnabled = false,
                sentryDsn = "https://<key>@sentry.io/<project>",
            ),
        localization =
            PluginConfig.Localization(
                timeZone = "UTC",
                locale = "en-us",
            ),
        chatBadge =
            PluginConfig.ChatBadge(
                icon = "<color:yellow>★</color>",
            ),
        warps =
            PluginConfig.Warps(
                itemsPerPage = 15,
            ),
        integrations =
            PluginConfig.Integrations(
                dynmap =
                    PluginConfig.Integrations.Dynmap(
                        warpIconName = "portal",
                    ),
            ),
        announcements =
            PluginConfig.Announcements(
                intervalInMins = 30,
                messages =
                    listOf(
                        "<color:aqua>Join the Project City Build Discord server! Type /discord in game!</color>",
                        "<color:aqua>Donations are the only way to keep PCB running! If you would like to donate to the server, you can do so by typing /donate in game!</color>",
                        "<color:aqua>Vote for us to help keep PCB active! Type /vote in game!</color>",
                        "<color:aqua>Post screenshots of your builds to the #showcase channel on our Discord to be featured on the PCB Instagram! Type /discord to join!</color>",
                        "<color:aqua>Make sure to follow the Project City Build Instagram for features of YOUR builds! Type /instagram in game!</color>",
                    ),
            ),
        groups =
            PluginConfig.Groups(
                displayPriority =
                    PluginConfig.Groups.DisplayPriority(
                        builder =
                            listOf(
                                "architect",
                                "engineer",
                                "planner",
                                "builder",
                                "intern",
                            ),
                        trust =
                            listOf(
                                "developer",
                                "moderator",
                                "trusted+",
                                "trusted",
                                "member",
                            ),
                        donor =
                            listOf(
                                "donator",
                                "legacy-donator",
                            ),
                    ),
                donorTierGroupNames =
                    PluginConfig.Groups.DonorTierGroupNames(
                        copper = "copper_tier",
                        iron = "iron_tier",
                        diamond = "diamond_tier",
                    ),
            ),
        messages =
            PluginConfig.Messages(
                join = "<color:green><b>+</b></color> %name% <color:gray>joined the server</color>",
                leave = "<color:red><b>-</b></color> %name% <color:gray>left the server (online for %time_online%)</color>",
                firstTimeJoin = "<color:AA00AA><b>✦ Welcome §f%name%§d to the server!</b></color>",
                welcome = "<b>Welcome to PCB!</b>",
            ),
    )
