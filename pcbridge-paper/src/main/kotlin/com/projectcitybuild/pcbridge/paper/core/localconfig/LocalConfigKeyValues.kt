@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.paper.core.localconfig

import kotlinx.serialization.Serializable

/**
 * Key-values for the plugin configured through the local filesystem
 */
@Serializable
data class LocalConfigKeyValues(
    val api: Api,
    val database: Database,
    val webServer: WebServer,
    val errorReporting: ErrorReporting,
    val groups: Groups,
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

fun LocalConfigKeyValues.default() =
    LocalConfigKeyValues(
        api =
            LocalConfigKeyValues.Api(
                token = "FILL_THIS_IN",
                baseUrl = "https://projectcitybuild.com/api/",
                isLoggingEnabled = false,
            ),
        database =
            LocalConfigKeyValues.Database(
                hostName = "127.0.0.1",
                port = 3306,
                name = "pcbridge",
                username = "FILL_THIS_IN",
                password = "FILL_THIS_IN",
            ),
        webServer =
            LocalConfigKeyValues.WebServer(
                token = "FILL_THIS_IN",
                port = 8080,
            ),
        errorReporting =
            LocalConfigKeyValues.ErrorReporting(
                isSentryEnabled = false,
                sentryDsn = "https://<key>@sentry.io/<project>",
            ),
        groups =
            LocalConfigKeyValues.Groups(
                displayPriority =
                    LocalConfigKeyValues.Groups.DisplayPriority(
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
                    LocalConfigKeyValues.Groups.DonorTierGroupNames(
                        copper = "copper_tier",
                        iron = "iron_tier",
                        diamond = "diamond_tier",
                    ),
            ),
    )
