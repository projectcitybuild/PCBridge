package com.projectcitybuild.tests.integration.bans

import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.modules.bans.CreateBanAction
import junit.framework.TestCase.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader
import java.util.*

class CreateBanActionTests {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun jsonMockResponse(fileName: String): String {
        val reader = InputStreamReader(javaClass.classLoader.getResourceAsStream(fileName))
        val content = reader.readText()
        reader.close()
        return content
    }

    private fun networkClients(mockWebServer: MockWebServer): NetworkClients {
        val baseUrl = mockWebServer.url("")
        return NetworkClients(
                PCBClient(
                        authToken = "",
                        withLogging = false,
                        baseUrl = "$baseUrl/"
                ),
                MojangClient(
                        withLogging = false
                )
        )
    }

    private fun mockResponse(fileName: String): MockResponse {
        val json = jsonMockResponse(fileName)

        return MockResponse()
                .setBody(json)
                .setHeader("Content-Type", "application/json")
    }

    @Test
    fun `ban response returns success`() {
        val response = mockResponse("api_ban_status_ban_response.json")
        mockWebServer.enqueue(response)

        val networkClients = networkClients(mockWebServer)
        val action = CreateBanAction(networkClients)
        val result = action.execute(
                playerId = UUID.randomUUID(),
                playerName = "test_user",
                staffId = UUID.randomUUID(),
                reason = "test_reason"
        )
        assertThat(result, instanceOf(CreateBanAction.Result.SUCCESS::class.java))
    }

    @Test
    fun `invalid input returns failure`() {
        val response = mockResponse("api_ban_invalid_input_response.json")
        mockWebServer.enqueue(response)

        val networkClients = networkClients(mockWebServer)
        val action = CreateBanAction(networkClients)
        val result = action.execute(
                playerId = UUID.randomUUID(),
                playerName = "test_user",
                staffId = UUID.randomUUID(),
                reason = "test_reason"
        )
        if (result !is CreateBanAction.Result.FAILED) {
            return fail("Expected FAILED result but got $result")
        }
        assertEquals(result.reason, CreateBanAction.Failure.BAD_REQUEST)
    }

    @Test
    fun `banning an already-banned player returns failure`() {
        val response = mockResponse("api_ban_already_banned_response.json")
        mockWebServer.enqueue(response)

        val networkClients = networkClients(mockWebServer)
        val action = CreateBanAction(networkClients)
        val result = action.execute(
                playerId = UUID.randomUUID(),
                playerName = "test_user",
                staffId = UUID.randomUUID(),
                reason = "test_reason"
        )
        if (result !is CreateBanAction.Result.FAILED) {
            return fail("Expected FAILED result but got $result")
        }
        assertEquals(result.reason, CreateBanAction.Failure.PLAYER_ALREADY_BANNED)
    }
}