package com.projectcitybuild.modules.chat.listeners

import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class EmojiChatListenerTest {
    @Test
    fun `replaces placeholders with emoji unicode`() = runTest {
        mapOf(
            Pair(":SKULL:", "☠"),
            Pair(":sKuLl:", "☠"),
            Pair(":skull:", "☠"),
            Pair(":skull::skull:", "☠☠"),
            Pair(":skull:test", "☠test"),
            Pair(":skull:test:skull:", "☠test☠"),
            Pair(":skull::skull:test:skull:", "☠☠test☠"),
            Pair(":skull: :skull: ", "☠ ☠ "),
            Pair("skull:", "skull:"),
            Pair(":skull", ":skull"),
            Pair("skull", "skull"),
        ).forEach { (original, expected) ->
            val async = true
            val sender = mock(Player::class.java)
            val recipients = emptySet<Player>()

            val event = AsyncPlayerChatEvent(async, sender, original, recipients)
            EmojiChatListener().onAsyncPlayerChatEvent(event)

            assertEquals(event.message, expected)
        }
    }
}
