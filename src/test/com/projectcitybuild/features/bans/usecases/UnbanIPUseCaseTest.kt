package com.projectcitybuild.features.bans.usecases

class UnbanIPUseCaseTest {

//    private lateinit var useCase: UnbanIPUseCase
//
//    private lateinit var ipBanRepository: IPBanRepository
//
//    @BeforeEach
//    fun setUp() {
//        ipBanRepository = mock(IPBanRepository::class.java)
//
//        useCase = UnbanIPUseCase(ipBanRepository)
//    }
//
//    @Test
//    fun `should fail if IP is not banned`() = runTest {
//        val ip = "127.0.0.1"
//
//        `when`(ipBanRepository.get(ip)).thenReturn(null)
//
//        val result = useCase.unbanIP(ip)
//
//        assertEquals(result, Failure(UnbanIPUseCase.FailureReason.IP_NOT_BANNED))
//    }
//
//    @Test
//    fun `should fail if IP is invalid`() = runTest {
//        arrayOf(
//            "text",
//            "1234",
//        ).forEach { invalidIP ->
//            val result = useCase.unbanIP(invalidIP)
//
//            assertEquals(result, Failure(UnbanIPUseCase.FailureReason.INVALID_IP))
//        }
//    }
//
//    @Test
//    fun `should unban valid IP`() = runTest {
//        val ips = arrayOf(
//            "127.0.0.1",
//            "/127.0.0.1:1234", // This should get sanitized
//        )
//        ips.forEach { ip ->
//            `when`(ipBanRepository.get(ip)).thenReturn(IPBanMock())
//
//            val result = useCase.unbanIP(ip)
//
//            assertEquals(result, Success(Unit))
//        }
//        verify(ipBanRepository, times(ips.size)).delete("127.0.0.1")
//    }
}
