package com.projectcitybuild.modules.ranksync.actions

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import com.projectcitybuild.repositories.VerificationURLRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class GenerateAccountVerificationURLTest {

    private lateinit var verificationURLRepository: VerificationURLRepository
    private lateinit var useCase: GenerateAccountVerificationURL

    @BeforeEach
    fun setUp() {
        verificationURLRepository = mock(VerificationURLRepository::class.java)

        useCase = GenerateAccountVerificationURL(
            verificationURLRepository,
        )
    }

    @Test
    fun `should return verification URL if available`() = runTest {
        val playerUUID = UUID.randomUUID()
        val url = "https://pcbmc.co"

        whenever(verificationURLRepository.generateVerificationURL(playerUUID))
            .thenReturn(url)

        val result = useCase.generate(playerUUID)

        val expectedURL = GenerateAccountVerificationURL.VerificationURL(url)
        assertEquals(result, Success(expectedURL))
    }

    @Test
    fun `should return failure if verification URL is null`() = runTest {
        val playerUUID = UUID.randomUUID()

        whenever(verificationURLRepository.generateVerificationURL(playerUUID))
            .thenReturn(null)

        val result = useCase.generate(playerUUID)

        assertEquals(result, Failure(GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE))
    }

    @Test
    fun `should return failure if verification URL is empty`() = runTest {
        val playerUUID = UUID.randomUUID()

        whenever(verificationURLRepository.generateVerificationURL(playerUUID))
            .thenReturn("")

        val result = useCase.generate(playerUUID)

        assertEquals(result, Failure(GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE))
    }

    @Test
    fun `should return failure if account already linked`() = runTest {
        val playerUUID = UUID.randomUUID()

        whenever(verificationURLRepository.generateVerificationURL(playerUUID))
            .thenThrow(AccountLinkHTTPService.AlreadyLinkedException::class.java)

        val result = useCase.generate(playerUUID)

        assertEquals(result, Failure(GenerateAccountVerificationURL.FailureReason.ALREADY_LINKED))
    }
}
