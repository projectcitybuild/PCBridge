package com.projectcitybuild.tests.integration.bans

import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.tests.mocks.makeNetworkClients
import com.projectcitybuild.tests.mocks.withJSONResource
import junit.framework.TestCase.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
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

    @Test
    fun `ban response returns success with ban model`() {
        val response = MockResponse().withJSONResource("api_ban_status_ban_response.json")
        mockWebServer.enqueue(response)

        val action = CheckBanStatusAction(mockWebServer.makeNetworkClients())
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
        val response = MockResponse().withJSONResource("api_no_data_response.json")
        mockWebServer.enqueue(response)

        val action = CheckBanStatusAction(mockWebServer.makeNetworkClients())
        val result = action.execute(UUID.randomUUID())

        when (result) {
            is CheckBanStatusAction.Result.SUCCESS -> assertNull(result.ban)
            is CheckBanStatusAction.Result.FAILED -> fail("Expected SUCCESS result but received FAILED")
        }
    }
}