package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.http.APIClient
import com.projectcitybuild.core.http.APIClientMock
import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.clients.PCBClient
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.responses.ApiError
import com.projectcitybuild.entities.responses.ApiResponse
import com.projectcitybuild.entities.responses.AuthPlayerGroups
import com.projectcitybuild.entities.responses.Group
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.permissions.Permissions
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class UpdatePlayerGroupsUseCaseTest {

    private lateinit var useCase: UpdatePlayerGroupsUseCase

    private lateinit var apiRequestFactory: APIRequestFactory
    private lateinit var apiClient: APIClientMock
    private lateinit var permissions: Permissions
    private lateinit var config: PlatformConfig

    @BeforeEach
    fun setUp() {
        apiRequestFactory = mock(APIRequestFactory::class.java)
        apiClient = APIClientMock()
        permissions = mock(Permissions::class.java)
        config = mock(PlatformConfig::class.java)

        `when`(apiRequestFactory.pcb).thenReturn(mock(PCBClient::class.java))

        useCase = UpdatePlayerGroupsUseCase(
            apiRequestFactory,
            apiClient,
            permissions,
            config,
        )
    }

    private fun apiResponseMock(groups: List<Group>): ApiResponse<AuthPlayerGroups> {
        return ApiResponse(
            data = AuthPlayerGroups(
                id = "",
                email = "",
                username = "",
                groups = groups,
            ),
            error = null,
        )
    }

    private fun groupStub(id: Int, minecraftName: String?): Group {
        return Group(
            id = id,
            name = "",
            minecraftName = minecraftName,
            alias = null,
            _isAdmin = 0,
            _isStaff = 0,
            _isDefault = 0,
        )
    }

    @Test
    fun `should assign player to guest group if no groups`() = runTest {
        val playerUUID = UUID.randomUUID()

        `when`(config.get(ConfigKey.GROUPS_GUEST)).thenReturn("guest_group")

        apiClient.result = apiResponseMock(groups = emptyList())

        val result = useCase.sync(playerUUID)

        verify(permissions).setUserGroups(playerUUID, listOf("guest_group"))
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should assign player to given groups`() = runTest {
        val playerUUID = UUID.randomUUID()

        apiClient.result = apiResponseMock(
            groups = listOf(
                groupStub(1, "group1"),
                groupStub(2, "group2"),
                groupStub(3, null),
            )
        )

        val result = useCase.sync(playerUUID)

        verify(permissions).setUserGroups(playerUUID, listOf("group1", "group2"))
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should return failure if account not linked`() = runTest {
        val playerUUID = UUID.randomUUID()

        apiClient.exception = APIClient.HTTPError(
            errorBody = ApiError(
                id = "account_not_linked",
                title = "",
                detail = "",
                status = 1,
            )
        )

        val result = useCase.sync(playerUUID)

        verifyNoInteractions(permissions)
        assertEquals(result, Failure(UpdatePlayerGroupsUseCase.FailureReason.ACCOUNT_NOT_LINKED))
    }
}
