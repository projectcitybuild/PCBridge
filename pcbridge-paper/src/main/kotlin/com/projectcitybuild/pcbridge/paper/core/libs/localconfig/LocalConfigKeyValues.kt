@file:Suppress("ktlint:standard:max-line-length")

package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Key-values for the plugin configured through the local filesystem
 */
@Serializable
data class LocalConfigKeyValues(
    val environment: Environment,
    val api: Api,
    val webServer: WebServer,
    val observability: Observability,
    val discord: Discord,
) {
    @Serializable
    data class Api(
        val token: String,
        val baseUrl: String,
        val logLevel: LogLevel,
    ) {
        @Serializable
        enum class LogLevel {
            @SerializedName("none")
            None,
            @SerializedName("trace")
            Trace,
            @SerializedName("debug")
            Debug,
            @SerializedName("info")
            Info,
            ;

            val enabled: Boolean
                get() = this != None
        }
    }

    @Serializable
    data class WebServer(
        val token: String,
        val port: Int,
    )

    @Serializable
    data class Observability(
        val sentryDsn: String,
        val traceSampleRate: Double?,
    )

    @Serializable
    data class Discord(
        val contentAlertWebhook: String,
    )
}

fun LocalConfigKeyValues.Companion.default() =
    LocalConfigKeyValues(
        environment = Environment.DEV,
        api = LocalConfigKeyValues.Api(
            token = "pcbridge_local",
            baseUrl = "http://api.localhost/",
            logLevel = LocalConfigKeyValues.Api.LogLevel.Trace,
        ),
        webServer = LocalConfigKeyValues.WebServer(
            token = "pcbridge_local",
            port = 8080,
        ),
        observability = LocalConfigKeyValues.Observability(
            sentryDsn = "",
            traceSampleRate = 1.0,
        ),
        discord = LocalConfigKeyValues.Discord(
            contentAlertWebhook = ""
        )
    )
