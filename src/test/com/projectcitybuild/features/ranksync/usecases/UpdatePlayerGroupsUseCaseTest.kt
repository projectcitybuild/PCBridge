package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import com.projectcitybuild.modules.permissions.Permissions
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.util.*

class UpdatePlayerGroupsUseCaseTest {

    private lateinit var useCase: UpdatePlayerGroupsUseCase

    private lateinit var apiRequestFactory: APIRequestFactory
    private lateinit var apiClient: APIClient
    private lateinit var permissions: Permissions
    private lateinit var config: PlatformConfig

    @BeforeEach
    fun setUp() {

        apiRequestFactory = mock(APIRequestFactory::class.java)
        apiClient = mock(APIClient::class.java)
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

    @Test
    fun `should assign player to guest group if no groups`() = runTest {
        val playerUUID = UUID.randomUUID()

        `when`(apiClient.execute(any()))

        val result = useCase.sync(playerUUID)
    }
}