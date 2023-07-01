package com.projectcitybuild.features.aggregate.usecases

import com.projectcitybuild.features.aggregate.SyncPlayerWithAggregate
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigStorageKey
import com.projectcitybuild.modules.config.adapters.MemoryKeyValueStorage
import com.projectcitybuild.modules.permissions.Permissions
import com.projectcitybuild.pcbridge.core.PlatformLogger
import com.projectcitybuild.pcbridge.http.responses.Account
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.Badge
import com.projectcitybuild.pcbridge.http.responses.DonationPerk
import com.projectcitybuild.pcbridge.http.responses.DonationTier
import com.projectcitybuild.pcbridge.http.responses.Group
import com.projectcitybuild.repositories.ChatBadgeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

class SyncPlayerWithAggregateTest {

    private lateinit var useCase: SyncPlayerWithAggregate
    private lateinit var permissions: Permissions
    private lateinit var keyValueStorage: MemoryKeyValueStorage
    private lateinit var chatBadgeRepository: ChatBadgeRepository

    @BeforeEach
    fun setUp() {
        permissions = mock(Permissions::class.java)
        keyValueStorage = MemoryKeyValueStorage()
        chatBadgeRepository = mock(ChatBadgeRepository::class.java)

        useCase = SyncPlayerWithAggregate(
            permissions = permissions,
            config = Config(keyValueStorage),
            chatBadgeRepository = chatBadgeRepository,
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

    @Test
    fun `should set badges for player`() = runTest {
        val badges = listOf(
            Badge(displayName = "badge1"),
            Badge(displayName = "badge2"),
            Badge(displayName = "badge3"),
        )
        val aggregate = Aggregate(badges = badges)
        val uuid = UUID.randomUUID()

        useCase.execute(uuid, aggregate)

        verify(chatBadgeRepository).put(uuid, badges)
    }
}
