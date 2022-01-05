package com.projectcitybuild.tests.integration.bans

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