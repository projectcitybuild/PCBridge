package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.repositories.PlayerGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class UpdatePlayerGroupsUseCaseTest {

    private lateinit var useCase: UpdatePlayerGroupsUseCase

    private lateinit var permissions: Permissions
    private lateinit var playerGroupRepository: PlayerGroupRepository

    @BeforeEach
    fun setUp() {
        permissions = mock(Permissions::class.java)
        playerGroupRepository = mock(PlayerGroupRepository::class.java)

        useCase = UpdatePlayerGroupsUseCase(
            permissions,
            playerGroupRepository,
        )
    }

    @Test
    fun `should assign player to groups and donor tiers`() = runTest {
        val playerUUID = UUID.randomUUID()

        `when`(playerGroupRepository.getGroups(any())).thenReturn(
            listOf("group1", "group2")
        )
        `when`(playerGroupRepository.getDonorTiers(any())).thenReturn(
            listOf("donor_tier1")
        )

        val result = useCase.sync(playerUUID)

        verify(permissions).setUserGroups(playerUUID, listOf("group1", "group2", "donor_tier1"))
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should return failure if account not linked`() = runTest {
        val playerUUID = UUID.randomUUID()

        `when`(playerGroupRepository.getGroups(any())).thenThrow(
            PlayerGroupRepository.AccountNotLinkedException::class.java
        )
        `when`(playerGroupRepository.getDonorTiers(any())).thenReturn(emptyList())

        val result = useCase.sync(playerUUID)

        verifyNoInteractions(permissions)
        assertEquals(result, Failure(UpdatePlayerGroupsUseCase.FailureReason.ACCOUNT_NOT_LINKED))
    }
}
