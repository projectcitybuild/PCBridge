package com.projectcitybuild.pcbridge.core.support.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentExtensionsTest {
    private val plainText = PlainTextComponentSerializer.plainText()

    @Test
    fun `join places the separator between builders`() {
        val joined = listOf(
            Component.text("first").toBuilder(),
            Component.text("second").toBuilder(),
            Component.text("third").toBuilder(),
        ).join(Component.text(", ")).build()

        assertEquals("first, second, third", plainText.serialize(joined))
    }

    @Test
    fun `join does not add a separator for zero or one builder`() {
        val empty = emptyList<TextComponent.Builder>()
            .join(Component.text(", "))
            .build()

        val single = listOf(Component.text("only").toBuilder())
            .join(Component.text(", "))
            .build()

        assertEquals("", plainText.serialize(empty))
        assertEquals(
            "only",
            plainText.serialize(single),
        )
    }
}
