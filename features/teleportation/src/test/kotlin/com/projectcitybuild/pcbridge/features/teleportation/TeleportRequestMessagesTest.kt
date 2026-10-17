package com.projectcitybuild.pcbridge.features.teleportation

import com.projectcitybuild.pcbridge.core.l10n.l10n
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TeleportRequestMessagesTest {
    private val plainText = PlainTextComponentSerializer.plainText()

    @Test
    fun `incoming request messages have clickable accept and deny commands`() {
        listOf(
            l10n.teleportRequestReceived("Alex"),
            l10n.teleportHereRequestReceived("Alex"),
        ).forEach { message ->
            val commands = message.descendants().mapNotNull { component ->
                (component.clickEvent()?.payload() as? ClickEvent.Payload.Text)?.value()
            }

            assertTrue(plainText.serialize(message).contains("Alex"))
            assertTrue(plainText.serialize(message).contains("/tp accept"))
            assertTrue(plainText.serialize(message).contains("/tp deny"))
            assertEquals(listOf("/tp accept", "/tp deny"), commands)
        }
    }

    private fun Component.descendants(): List<Component> =
        listOf(this) + children().flatMap { it.descendants() }
}
