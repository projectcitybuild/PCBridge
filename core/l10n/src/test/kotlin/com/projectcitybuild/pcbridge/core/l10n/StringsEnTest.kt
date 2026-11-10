package com.projectcitybuild.pcbridge.core.l10n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StringsEnTest {
    private val strings = StringsEn()
    private val plainText = PlainTextComponentSerializer.plainText()

    @Test
    fun `successful message highlights dynamic parameter`() {
        val message = strings.builds.buildCreated("Archer's Tower")

        assertEquals("Archer's Tower created", plainText.serialize(message))
        assertEquals(TextColor.color(0x64D8FF), textNode(message, "Archer's Tower").style().color())
        assertEquals(TextColor.color(0x6BFF95), message.style().color())
    }

    @Test
    fun `error message keeps untrusted MiniMessage markup literal`() {
        val untrustedName = "<red>not red</red>"
        val message = strings.errors.errorWorldNotFound(untrustedName)

        assertEquals("Could not find world $untrustedName", plainText.serialize(message))
        assertEquals(TextColor.color(0x64D8FF), textNode(message, untrustedName).style().color())
        assertEquals(TextColor.color(0xFF6B6B), textNode(message, "Could not find world ").style().color())
    }

    @Test
    fun `every scalar in a multiple parameter message is highlighted`() {
        val message = strings.homes.homesListItem("Beach house", "world_nether", 12, 64, -8)

        assertEquals("Beach house (world_nether: 12, 64, -8)", plainText.serialize(message))
        listOf("Beach house", "world_nether", "12", "64", "-8").forEach { value ->
            assertEquals(TextColor.color(0x64D8FF), textNode(message, value).style().color(), value)
        }
    }

    @Test
    fun `world list uses status colors while highlighting world names`() {
        val message = strings.worlds.worldList(
            listOf(
                strings.worlds.worldListEntry("world", isLoaded = true),
                strings.worlds.worldListEntry("world_nether", isLoaded = false),
            ),
        )

        assertEquals(
            "Registered worlds: world (loaded), world_nether (unloaded)",
            plainText.serialize(message),
        )
        assertEquals(TextColor.color(0x64D8FF), textNode(message, "world").style().color())
        assertEquals(TextColor.color(0x6BFF95), textNode(message, " (loaded)").style().color())
        assertEquals(TextColor.color(0xFFD166), textNode(message, " (unloaded)").style().color())
    }

    @Test
    fun `numeric parameter is highlighted`() {
        val message = strings.tablist.playerPing(42)

        assertEquals(" 42", plainText.serialize(message))
        assertEquals(TextColor.color(0x64D8FF), textNode(message, "42").style().color())
    }

    @Test
    fun `gameplay messages accept hyphenated parameter names`() {
        val gameMode = strings.gameplay.playerGameModeChanged("Andy", "survival")
        val experienceAdded = strings.gameplay.playerExperienceAdded("Andy", 1000, 3000)
        val experienceSubtracted = strings.gameplay.playerExperienceSubtracted("Andy", 1000, 3000)

        assertEquals("Changed Andy's game mode to survival", plainText.serialize(gameMode))
        assertEquals(
            "Added 1000 experience points to Andy. They now have 3000 experience points.",
            plainText.serialize(experienceAdded),
        )
        assertEquals(
            "Subtracted 1000 experience points from Andy. They now have 3000 experience points.",
            plainText.serialize(experienceSubtracted),
        )
        listOf(
            textNode(gameMode, "survival"),
            textNode(experienceAdded, "3000"),
            textNode(experienceSubtracted, "3000"),
        ).forEach { parameter ->
            assertEquals(TextColor.color(0x64D8FF), parameter.style().color())
        }
    }

    @Test
    fun `component placeholder preserves its existing formatting`() {
        val formattedName = Component.text("Custom name", NamedTextColor.BLUE).decorate(TextDecoration.BOLD)
        val message = strings.building.renamedItem(formattedName)
        val insertedName = textNode(message, "Custom name")

        assertEquals("Renamed item in hand to Custom name", plainText.serialize(message))
        assertEquals(NamedTextColor.BLUE, insertedName.style().color())
        assertTrue(insertedName.style().hasDecoration(TextDecoration.BOLD))
    }

    @Test
    fun `ban edit link is clickable and consistently styled`() {
        val url = "https://example.test/ban/123"
        val message = strings.bans.clickToEditBan(url)
        val link = textNode(message, "here")

        assertEquals("Click here to edit the ban or add additional information", plainText.serialize(message))
        assertEquals(TextColor.color(0xA78BFA), link.style().color())
        assertTrue(link.style().hasDecoration(TextDecoration.UNDERLINED))
        assertEquals(ClickEvent.Action.OPEN_URL, link.clickEvent()?.action())
        assertEquals(url, (link.clickEvent()?.payload() as? ClickEvent.Payload.Text)?.value())
        assertNotNull(link.hoverEvent())
        val hover = link.hoverEvent()!!.value() as Component
        assertEquals(url, plainText.serialize(hover))
        assertEquals(TextColor.color(0x64D8FF), textNode(hover, url).style().color())
    }

    @Test
    fun `build and teleport interactions keep their commands`() {
        val buildName = textNode(strings.builds.buildCreatedAnnouncement("Player", "Sky Castle"), "Sky Castle")
        val acceptCommand = textNode(strings.teleportation.teleportRequestReceived("Player"), "/tp accept")

        assertEquals(TextColor.color(0x64D8FF), buildName.style().color())
        assertTrue(buildName.style().hasDecoration(TextDecoration.UNDERLINED))
        assertEquals("/build Sky Castle", textClickPayload(buildName))
        assertEquals("/tp accept", textClickPayload(acceptCommand))
        assertEquals(TextColor.color(0xA78BFA), acceptCommand.style().color())
        assertTrue(acceptCommand.style().hasDecoration(TextDecoration.UNDERLINED))
    }

    private fun textNode(
        component: Component,
        content: String,
    ): TextComponent = descendants(component)
        .filterIsInstance<TextComponent>()
        .first { it.content() == content }

    private fun descendants(component: Component): Sequence<Component> =
        sequenceOf(component) + component.children().asSequence().flatMap(::descendants)

    private fun textClickPayload(component: Component): String? = (component.clickEvent()?.payload() as? ClickEvent.Payload.Text)?.value()
}
