package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import com.projectcitybuild.repositories.PlayerGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class UpdatePlayerGroupsTest {

    private lateinit var useCase: UpdatePlayerGroups

    private lateinit var permissions: Permissions
    private lateinit var playerGroupRepository: PlayerGroupRepository

    @BeforeEach
    fun setUp() {
        permissions = mock(Permissions::class.java)
        playerGroupRepository = mock(PlayerGroupRepository::class.java)

        useCase = UpdatePlayerGroups(
            permissions,
            playerGroupRepository,
        )
    }

    @Test
    fun `should assign player to groups and donor tiers`() = runTest {
        val playerUUID = UUID.randomUUID()

        whenever(playerGroupRepository.getGroups(any())).thenReturn(
            listOf("group1", "group2")
        )
        whenever(playerGroupRepository.getDonorTiers(any())).thenReturn(
            listOf("donor_tier1")
        )

        val result = useCase.execute(playerUUID)

        verify(permissions).setUserGroups(playerUUID, listOf("group1", "group2", "donor_tier1"))
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should return failure if account not linked`() = runTest {
        val playerUUID = UUID.randomUUID()

        whenever(playerGroupRepository.getGroups(any())).thenThrow(
            PlayerGroupHttpService.NoLinkedAccountException::class.java
        )
        whenever(playerGroupRepository.getDonorTiers(any())).thenReturn(emptyList())

        val result = useCase.execute(playerUUID)

        verifyNoInteractions(permissions)
        assertEquals(result, Failure(UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED))
    }
}
