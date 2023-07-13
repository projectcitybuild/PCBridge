package com.projectcitybuild.modules.ranksync.actions

import com.projectcitybuild.libs.config.Config
import com.projectcitybuild.libs.config.ConfigStorageKey
import com.projectcitybuild.libs.config.adapters.MemoryKeyValueStorage
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

class SyncPlayerGroupsWithAggregateTest {

    private lateinit var useCase: SyncPlayerGroupsWithAggregate
    private lateinit var permissions: Permissions
    private lateinit var keyValueStorage: MemoryKeyValueStorage

    @BeforeEach
    fun setUp() {
        permissions = mock(Permissions::class.java)
        keyValueStorage = MemoryKeyValueStorage()

        useCase = SyncPlayerGroupsWithAggregate(
            permissions = permissions,
            config = Config(keyValueStorage),
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

        keyValueStorage.set(
            key = ConfigStorageKey(path = "donors.tiers.copper_tier.permission_group_name", defaultValue = "default"),
            value = "tier1",
        )

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
