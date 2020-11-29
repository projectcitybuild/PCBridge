package com.projectcitybuild.tests.integration.bans

import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import junit.framework.TestCase.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader
import java.util.*

class CheckBanStatusActionTests {

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
    fun `ban response returns success with ban model`() {
        val response = mockResponse("api_ban_status_ban_response.json")
        mockWebServer.enqueue(response)

        val networkClients = networkClients(mockWebServer)
        val action = CheckBanStatusAction(networkClients)
        val result = action.execute(UUID.randomUUID())

        if (result !is CheckBanStatusAction.Result.SUCCESS) {
            return fail("Expected SUCCESS result but got $result")
        }
        if (result.ban == null) {
            return fail("Expected GameBan model but received null")
        }
        assertEquals(result.ban, GameBan(
                id = 111,
                serverId = 222,
                playerId = "333",
                playerAlias = "fake_user",
                playerType = "minecraft_player",
                reason = "fake_ban_reason",
                staffId = "444",
                staffType = "minecraft_player",
                isActive = true,
                isGlobalBan = true,
                expiresAt = null,
                createdAt = 1606647622,
                updatedAt = 1606647633
        ))
    }

    @Test
    fun `empty response returns success with no ban model`() {
        val response = mockResponse("api_no_data_response.json")
        mockWebServer.enqueue(response)

        val networkClients = networkClients(mockWebServer)
        val action = CheckBanStatusAction(networkClients)
        val result = action.execute(UUID.randomUUID())

        when (result) {
            is CheckBanStatusAction.Result.SUCCESS -> assertNull(result.ban)
            is CheckBanStatusAction.Result.FAILED -> fail("Expected SUCCESS result but received FAILED")
        }
    }
}