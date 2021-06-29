package com.projectcitybuild.tests.integration.bans

import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.tests.mocks.makeNetworkClients
import com.projectcitybuild.tests.mocks.withJSONResource
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class CreateBanActionTests {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `ban response returns success`() {
        val response = MockResponse().withJSONResource("api_ban_status_ban_response.json")
        mockWebServer.enqueue(response)

        val action = CreateBanAction(mockWebServer.makeNetworkClients())
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
        val response = MockResponse().withJSONResource("api_ban_invalid_input_response.json")
        mockWebServer.enqueue(response)

        val action = CreateBanAction(mockWebServer.makeNetworkClients())
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
        val response = MockResponse().withJSONResource("api_ban_already_banned_response.json")
        mockWebServer.enqueue(response)

        val action = CreateBanAction(mockWebServer.makeNetworkClients())
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