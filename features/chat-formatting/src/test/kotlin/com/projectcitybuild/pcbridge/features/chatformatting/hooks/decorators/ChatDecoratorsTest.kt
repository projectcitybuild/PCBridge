package com.projectcitybuild.pcbridge.features.chatformatting.hooks.decorators

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.chatformatting.domain.repositories.EmojiRepository
import com.projectcitybuild.pcbridge.runtime.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ChatDecoratorsTest {
    @Test
    fun `emoji decorator replaces placeholders without changing surrounding text`() =
        runTest {
            val remoteConfig = mock(RemoteConfig::class.java)
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    version = 1,
                    config = RemoteConfigKeyValues(emojis = mapOf(":wave:" to "👋")),
                ),
            )
            val repository = EmojiRepository(remoteConfig)
            val player = mock(Player::class.java)
            val message = ChatMessage(player, Component.text("Hi :WAVE:!"))

            val result = ChatEmojiDecorator(repository).decorate(message)

            assertEquals("Hi 👋!", result.message.plainText())
        }

    @Test
    fun `url decorator adds https to bare domains and preserves explicit schemes`() =
        runTest {
            val player = mock(Player::class.java)
            val message =
                ChatMessage(
                    player,
                    Component.text("Visit example.com/docs or https://projectcitybuild.com"),
                )

            val result = ChatUrlDecorator().decorate(message)

            assertEquals(
                "Visit example.com/docs or https://projectcitybuild.com",
                result.message.plainText(),
            )
            assertEquals(
                setOf("https://example.com/docs", "https://projectcitybuild.com"),
                result.message.clickEventValues(),
            )
        }

    @Test
    fun `url decorator leaves messages without urls unchanged`() =
        runTest {
            val message = ChatMessage(mock(Player::class.java), Component.text("No link here"))

            val result = ChatUrlDecorator().decorate(message)

            assertEquals("No link here", result.message.plainText())
            assertTrue(result.message.clickEventValues().isEmpty())
        }

    private fun Component.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this)

    private fun Component.clickEventValues(): Set<String> =
        descendants().mapNotNull { component ->
            val clickEvent = component.clickEvent()?.takeIf { it.action() == ClickEvent.Action.OPEN_URL }
            val payload = clickEvent?.payload() as? ClickEvent.Payload.Text
            payload?.value()
        }.toSet()

    private fun Component.descendants(): List<Component> = listOf(this) + children().flatMap { it.descendants() }
}
