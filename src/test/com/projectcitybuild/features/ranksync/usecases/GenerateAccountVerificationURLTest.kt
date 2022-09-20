package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.clients.PCBClient
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.core.http.core.APIClientMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.ApiError
import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.AuthURL
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class GenerateAccountVerificationURLTest {

    private lateinit var useCase: GenerateAccountVerificationURL

    private lateinit var apiRequestFactory: APIRequestFactory
    private lateinit var apiClient: APIClientMock

    @BeforeEach
    fun setUp() {
        apiRequestFactory = mock(APIRequestFactory::class.java)
        apiClient = APIClientMock()

        `when`(apiRequestFactory.pcb).thenReturn(mock(PCBClient::class.java))

        useCase = GenerateAccountVerificationURL(
            apiRequestFactory,
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

        apiClient.exception = APIClient.HTTPError(
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