package com.projectcitybuild.features.bans.usecases

class BanIPTest {

//    private lateinit var useCase: BanIPUseCase
//
//    private lateinit var ipBanRepository: IPBanRepository
//    private lateinit var playerKicker: PlayerKicker
//    private lateinit var time: Time

//    @BeforeEach
//    fun setUp() {
//        ipBanRepository = mock(IPBanRepository::class.java)
//        playerKicker = mock(PlayerKicker::class.java)
//        time = mock(Time::class.java)
//
//        useCase = BanIPUseCase(
//            ipBanRepository,
//            playerKicker,
//            time,
//        )
//    }

//    @Test
//    fun `should fail if IP is already banned`() = runTest {
//        val ip = "127.0.0.1"
//
//        `when`(ipBanRepository.get(ip)).thenReturn(IPBanMock())
//
//        val result = useCase.banIP(ip, null, null)
//
//        assertEquals(result, Failure(BanIPUseCase.FailureReason.IP_ALREADY_BANNED))
//    }
//
//    @Test
//    fun `should fail if IP is invalid`() = runTest {
//        arrayOf(
//            "text",
//            "1234",
//        ).forEach { invalidIP ->
//            val result = useCase.banIP(invalidIP, null, null)
//
//            assertEquals(result, Failure(BanIPUseCase.FailureReason.INVALID_IP))
//        }
//    }
//
//    @Test
//    fun `should ban valid IP`() = runTest {
//        val ips = arrayOf(
//            "127.0.0.1",
//            "/127.0.0.1:1234", // This should get sanitized
//        )
//        ips.forEach { ip ->
//            val now = LocalDateTime.now()
//
//            `when`(ipBanRepository.get(ip)).thenReturn(null)
//            `when`(time.now()).thenReturn(now)
//
//            val result = useCase.banIP(ip, "banner_name", "reason")
//
//            val expectedBan = IPBan(
//                "127.0.0.1",
//                "banner_name",
//                "reason",
//                now,
//            )
//            verify(ipBanRepository).put(expectedBan)
//
//            assertEquals(result, Success(Unit))
//        }
//    }
//
//    @Test
//    fun `should ban valid IP without reason or banner name given`() = runTest {
//        val ip = "127.0.0.1"
//        val now = LocalDateTime.now()
//
//        `when`(ipBanRepository.get(ip)).thenReturn(null)
//        `when`(time.now()).thenReturn(now)
//
//        val result = useCase.banIP(ip, null, null)
//
//        val expectedBan = IPBan(
//            "127.0.0.1",
//            null,
//            "",
//            now,
//        )
//        verify(ipBanRepository).put(expectedBan)
//
//        assertEquals(result, Success(Unit))
//    }
//
//    @Test
//    fun `should kick player if online`() = runTest {
//        val ip = "127.0.0.1"
//
//        `when`(ipBanRepository.get(ip)).thenReturn(null)
//        `when`(time.now()).thenReturn(LocalDateTime.now())
//
//        val result = useCase.banIP(ip, "banner_name", "reason")
//
//        verify(playerKicker).kickByIP(eq(ip), anyString(), eq(PlayerKicker.KickContext.FATAL))
//
//        assertEquals(result, Success(Unit))
//    }
}