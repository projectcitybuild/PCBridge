package com.projectcitybuild.modules.ranksync.actions

import com.projectcitybuild.ConfigData
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.http.responses.Account
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.responses.DonationTier
import com.projectcitybuild.pcbridge.http.responses.Group
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.util.UUID

class SyncPlayerGroupsWithAggregateTest {

    @Mock
    private lateinit var permissions: Permissions

    @Mock
    private lateinit var config: Config<ConfigData>

    private lateinit var useCase: SyncPlayerGroupsWithAggregate

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        useCase = SyncPlayerGroupsWithAggregate(
            permissions = permissions,
            config = config,
            logger = mock(PlatformLogger::class.java),
        )
    }

    @Test
    fun `should set groups for player`() = runTest {
        val account = Account(
            groups = listOf(
                Group(minecraftName = "group1"),
                Group(minecraftName = "group2"),
            )
        )
        val donorPerk = DonationPerk(donationTier = DonationTier(name = "copper_tier"))
        val aggregate = Aggregate(account = account, donationPerks = listOf(donorPerk))
        val uuid = UUID.randomUUID()

        whenever(config.get()).thenReturn(ConfigData.default.copy(
            groups = ConfigData.default.groups.copy(
                donorTierGroupNames = ConfigData.Groups.DonorTierGroupNames(
                    copper = "tier1",
                    iron = "tier2",
                    diamond = "tier3",
                ),
            ),
        ))

        useCase.execute(uuid, aggregate)

        verify(permissions).setUserGroups(uuid, listOf("group1", "group2", "tier1"))
    }

    @Test
    fun `should set groups to nothing for guests`() = runTest {
        val account = Account(groups = emptyList())
        val aggregate = Aggregate(account = account, donationPerks = emptyList())
        val uuid = UUID.randomUUID()

        useCase.execute(uuid, aggregate)

        verify(permissions).setUserGroups(uuid, listOf())
    }
}
