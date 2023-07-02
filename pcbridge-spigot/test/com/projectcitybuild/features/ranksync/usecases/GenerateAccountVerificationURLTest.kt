package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.parsing.MockResponseParser
import com.projectcitybuild.pcbridge.http.parsing.ApiError
import com.projectcitybuild.pcbridge.http.parsing.ApiResponse
import com.projectcitybuild.pcbridge.http.responses.AuthURL
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import java.util.UUID

class GenerateAccountVerificationURLTest {

    private lateinit var useCase: GenerateAccountVerificationURL

    private lateinit var pcbClient: PCBClientFactory
    private lateinit var apiClient: MockResponseParser

    @BeforeEach
    fun setUp() {
        pcbClient = mock(PCBClientFactory::class.java)
        apiClient = MockResponseParser()

        useCase = GenerateAccountVerificationURL(
            pcbClient,
            apiClient,
        )
    }

    @Test
    fun `should return verification URL if available`() = runTest {
        val playerUUID = UUID.randomUUID()
        val url = "https://pcbmc.co"

        apiClient.result = ApiResponse(
            data = AuthURL(url),
            error = null,
        )

        val result = useCase.generate(playerUUID)

        val expectedURL = GenerateAccountVerificationURL.VerificationURL(url)
        assertEquals(result, Success(expectedURL))
    }

    @Test
    fun `should return failure if verification URL is empty`() = runTest {
        val playerUUID = UUID.randomUUID()

        apiClient.result = ApiResponse(
            data = AuthURL(""),
            error = null,
        )

        val result = useCase.generate(playerUUID)

        assertEquals(result, Failure(GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE))
    }

    @Test
    fun `should return failure if account already linked`() = runTest {
        val playerUUID = UUID.randomUUID()

        apiClient.exception = ResponseParser.HTTPError(
            errorBody = ApiError(
                id = "already_authenticated",
                title = "",
                detail = "",
                status = 1,
            )
        )

        val result = useCase.generate(playerUUID)

        assertEquals(result, Failure(GenerateAccountVerificationURL.FailureReason.ALREADY_LINKED))
    }
}
