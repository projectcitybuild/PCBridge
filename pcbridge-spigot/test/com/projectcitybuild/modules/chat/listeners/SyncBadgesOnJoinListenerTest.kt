package com.projectcitybuild.modules.chat.listeners

import com.projectcitybuild.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.Badge
import com.projectcitybuild.repositories.ChatBadgeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

class SyncBadgesOnJoinListenerTest {

    @Test
    fun `should set badges for player`() = runTest {
        val badges = listOf(
            Badge(displayName = "badge1"),
            Badge(displayName = "badge2"),
            Badge(displayName = "badge3"),
        )
        val aggregate = Aggregate(badges = badges)
        val uuid = UUID.randomUUID()
        val event = ConnectionPermittedEvent(aggregate, uuid)

        val chatBadgeRepository = mock(ChatBadgeRepository::class.java)

        SyncBadgesOnJoinListener(chatBadgeRepository).handle(event)

        verify(chatBadgeRepository).put(uuid, badges)
    }
}
