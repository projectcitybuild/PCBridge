package com.projectcitybuild.pcbridge.webserver.routes

import com.projectcitybuild.pcbridge.webserver.WebhookDelegate
import com.projectcitybuild.pcbridge.webserver.data.PlayerSyncRequestedWebhook
import com.projectcitybuild.pcbridge.webserver.testEnvironment
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import java.util.UUID

class PlayerRoutesTest {
    private val route = "events/player/sync"

    private lateinit var webhookDelegate: WebhookDelegate

    @BeforeEach
    fun setUp() {
        webhookDelegate = mock(WebhookDelegate::class.java)
    }

    @Test
    fun `requires auth`() = testEnvironment(webhookDelegate) { env ->
        val noAuthResponse = client.post(route)
        assertEquals(HttpStatusCode.Unauthorized, noAuthResponse.status)

        val authedResponse = client.post(route) {
            bearerAuth(env.validToken)
        }
        assertNotEquals(HttpStatusCode.Unauthorized, authedResponse.status)
    }

    @Test
    fun `calls webhook delegate and returns 200`() = testEnvironment(webhookDelegate) { env ->
        val response = client.post(route) {
            bearerAuth(env.validToken)
            contentType(ContentType.Application.Json)
            setBody("""
                {"uuid": "069a79f444e94726a5befca90e38aaf5"}
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val expectedEvent = PlayerSyncRequestedWebhook(
            playerUUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
        )
        verify(webhookDelegate).handle(expectedEvent)
    }

    @Test
    fun `invalid uuid returns 400`() = testEnvironment(webhookDelegate) { env ->
        val response = client.post(route) {
            bearerAuth(env.validToken)
            contentType(ContentType.Application.Json)
            setBody("""
                {"uuid": "invalid"}
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `invalid body returns 400`() = testEnvironment(webhookDelegate) { env ->
        val response = client.post(route) {
            bearerAuth(env.validToken)
            contentType(ContentType.Application.Json)
            setBody("""
                {"unexpected"}
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}