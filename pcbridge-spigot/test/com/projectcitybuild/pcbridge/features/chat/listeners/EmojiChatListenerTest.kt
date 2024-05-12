// package com.projectcitybuild.pcbridge.features.chat.listeners
//
// import com.projectcitybuild.pcbridge.features.chat.listeners.EmojiChatListener
// import io.papermc.paper.event.player.AsyncChatEvent
// import kotlinx.coroutines.test.runTest
// import org.bukkit.entity.Player
// import org.bukkit.event.player.AsyncPlayerChatEvent
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.Test
// import org.mockito.Mockito.mock
//
// class EmojiChatListenerTest {
//
//     @Test
//     fun `replaces every supported emoji`() = runTest {
//         mapOf(
//             Pair(":skull:", "☠"),
//             Pair(":heart:", "❤"),
//             Pair(":fire:", "\uD83D\uDD25"),
//             Pair(":tm:", "™"),
//         ).forEach { (original, expected) ->
//             val async = true
//             val sender = mock(Player::class.java)
//             val recipients = emptySet<Player>()
//
//             val event = AsyncChatEvent(async, sender, original, recipients)
//             EmojiChatListener().onChat(event)
//
//             assertEquals(expected, event.message)
//         }
//     }
//
//     @Test
//     fun `replaces supported emojis in all patterns without case sensitivity`() = runTest {
//         mapOf(
//             Pair(":SKULL:", "☠"),
//             Pair(":sKuLl:", "☠"),
//             Pair(":skull:", "☠"),
//             Pair(":skull::skull:", "☠☠"),
//             Pair(":skull:test", "☠test"),
//             Pair(":skull:test:skull:", "☠test☠"),
//             Pair(":skull::skull:test:skull:", "☠☠test☠"),
//             Pair(":skull: :skull: ", "☠ ☠ "),
//             Pair("skull:", "skull:"),
//             Pair(":skull", ":skull"),
//             Pair("skull", "skull"),
//             Pair(":skull::heart:", "☠❤"),
//             Pair(":skull: :heart:", "☠ ❤"),
//         ).forEach { (original, expected) ->
//             val async = true
//             val sender = mock(Player::class.java)
//             val recipients = emptySet<Player>()
//
//             val event = AsyncPlayerChatEvent(async, sender, original, recipients)
//             EmojiChatListener().handle(event)
//
//             assertEquals(expected, event.message)
//         }
//     }
// }
