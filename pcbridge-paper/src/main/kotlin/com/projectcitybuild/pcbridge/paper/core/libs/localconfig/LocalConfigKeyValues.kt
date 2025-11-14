@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import kotlinx.serialization.Serializable

/**
 * Key-values for the plugin configured through the local filesystem
 */
@Serializable
data class LocalConfigKeyValues(
    val api: Api,
    val webServer: WebServer,
    val errorReporting: ErrorReporting,
    val discord: Discord,
) {
    @Serializable
    data class Api(
        val token: String,
        val baseUrl: String,
        val isLoggingEnabled: Boolean,
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
    data class Discord(
        val contentAlertWebhook: String,
    )
}

fun LocalConfigKeyValues.Companion.default() =
    LocalConfigKeyValues(
        api =
            LocalConfigKeyValues.Api(
                token = "FILL_THIS_IN",
                baseUrl = "https://projectcitybuild.com/api/",
                isLoggingEnabled = false,
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
        discord = LocalConfigKeyValues.Discord(
            contentAlertWebhook = "FILL_THIS_IN"
        )
    )
