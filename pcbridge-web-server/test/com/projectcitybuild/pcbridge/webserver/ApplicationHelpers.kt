package com.projectcitybuild.pcbridge.webserver

import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

internal fun testEnvironment(
    webhookDelegate: WebhookDelegate,
    test: suspend ApplicationTestBuilder.(TestEnv) -> Unit,
) = testApplication {
    application {
        configureContentNegotiation()
        configureAuthentication(authToken = "token")
        configureRouting(webhookDelegate = webhookDelegate)
    }
    test(TestEnv())
}

internal class TestEnv {
    val validToken = "token"
}