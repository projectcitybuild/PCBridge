package com.projectcitybuild.pcbridge.features.chatbadge.domain

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Badge
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ChatBadgeFormatterTest {
    private val formatter = ChatBadgeFormatter()
    private val plainTextSerializer = PlainTextComponentSerializer.plainText()

    @Test
    fun `returns no component when the player has no badges`() {
        assertNull(formatter.format(emptyList(), "<yellow>★</yellow>"))
    }

    @Test
    fun `formats the configured icon with badge details in its hover text`() {
        val badges =
            listOf(
                Badge(displayName = "Builder", unicodeIcon = "◆"),
                Badge(displayName = "Helper", unicodeIcon = "✦"),
            )

        val result = formatter.format(badges, "<yellow>★</yellow>")

        val component = requireNotNull(result)
        assertEquals("★", plainTextSerializer.serialize(component))
        val hoverText = requireNotNull(component.hoverEvent()).value() as Component
        assertEquals("Badges\n---\n◆ Builder\n✦ Helper", plainTextSerializer.serialize(hoverText))
        assertEquals(HoverEvent.Action.SHOW_TEXT, component.hoverEvent()?.action())
    }
}
