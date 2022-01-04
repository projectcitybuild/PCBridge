package com.projectcitybuild.tests.integration.bans

import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.bans.CreateUnbanAction
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

class CreateUnbanActionTests {

//    private lateinit var mockWebServer: MockWebServer
//
//    @Before
//    fun setup() {
//        mockWebServer = MockWebServer()
//        mockWebServer.start()
//    }
//
//    @After
//    fun tearDown() {
//        mockWebServer.shutdown()
//    }
//
//    @Test
//    fun `unban response returns success`() {
//        val response = MockResponse().withJSONResource("api_ban_unban_response.json")
//        mockWebServer.enqueue(response)
//
//        val action = CreateUnbanAction(mockWebServer.makeNetworkClients())
//        val result = action.execute(
//                playerId = UUID.randomUUID(),
//                staffId = UUID.randomUUID()
//        )
//        assertThat(result, instanceOf(CreateUnbanAction.Result.SUCCESS::class.java))
//    }
//
//    @Test
//    fun `invalid input returns failure`() {
//        val response = MockResponse().withJSONResource("api_ban_invalid_input_response.json")
//        mockWebServer.enqueue(response)
//
//        val action = CreateUnbanAction(mockWebServer.makeNetworkClients())
//        val result = action.execute(
//                playerId = UUID.randomUUID(),
//                staffId = UUID.randomUUID()
//        )
//        if (result !is CreateUnbanAction.Result.FAILED) {
//            return fail("Expected FAILED result but got $result")
//        }
//        assertEquals(result.reason, CreateUnbanAction.Failure.BAD_REQUEST)
//    }
//
//    @Test
//    fun `unbanning a player that's not banned returns failure`() {
//        val response = MockResponse().withJSONResource("api_ban_not_banned_response.json")
//        mockWebServer.enqueue(response)
//
//        val action = CreateUnbanAction(mockWebServer.makeNetworkClients())
//        val result = action.execute(
//                playerId = UUID.randomUUID(),
//                staffId = UUID.randomUUID()
//        )
//        if (result !is CreateUnbanAction.Result.FAILED) {
//            return fail("Expected FAILED result but got $result")
//        }
//        assertEquals(result.reason, CreateUnbanAction.Failure.PLAYER_NOT_BANNED)
//    }
}