package com.projectcitybuild.pcbridge.core.support.component

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AudienceExtensionsTest {
    @Test
    fun `sendMessageRich parses MiniMessage formatting before sending`() {
        val audience = mock<Audience>()

        audience.sendMessageRich("<green>Hello</green>")

        verify(audience).sendMessage(Component.text("Hello", NamedTextColor.GREEN))
    }
}
