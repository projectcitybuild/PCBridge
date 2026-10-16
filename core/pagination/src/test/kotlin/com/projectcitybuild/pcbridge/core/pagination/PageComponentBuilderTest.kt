package com.projectcitybuild.pcbridge.core.pagination

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PaginatedList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PageComponentBuilderTest {
    private val builder = PageComponentBuilder()
    private val plainText = PlainTextComponentSerializer.plainText()

    @Test
    fun `build should render items and page navigation with click and hover events`() {
        val requestedPages = mutableListOf<Int>()

        val component = builder.build(
            title = "Warp List",
            items = listOf("alpha", "beta"),
            pageNumber = 2,
            totalPages = 3,
            pageCommand = { page ->
                requestedPages.add(page)
                "/warps list $page"
            },
            itemClickCommand = { "/warp $it" },
            itemHover = { "Teleport to $it" },
            itemDecorator = { "<aqua><u>$it</u></aqua>" },
        )

        val rendered = plainText.serialize(component)
        val descendants = component.descendants()
        val clickCommands = descendants.mapNotNull { component ->
            (component.clickEvent()?.payload() as? ClickEvent.Payload.Text)?.value()
        }

        assertTrue(rendered.contains("--- Warp List - Page 2/3 ---"))
        assertTrue(rendered.contains("alpha"))
        assertTrue(rendered.contains("beta"))
        assertTrue(rendered.contains("[← Prev]"))
        assertTrue(rendered.contains("[Next →]"))
        assertEquals(listOf(1, 3), requestedPages)
        assertTrue(clickCommands.containsAll(listOf("/warp alpha", "/warp beta", "/warps list 1", "/warps list 3")))
        assertEquals(4, descendants.count { it.hoverEvent()?.action() == HoverEvent.Action.SHOW_TEXT })
    }

    @Test
    fun `build paginated list should derive total pages from total and page size`() {
        val paginated = PaginatedList(
            data = listOf("gamma", "delta"),
            currentPage = 2,
            perPage = 2,
            to = 4,
            total = 5,
        )

        val component = builder.build(
            title = "Items",
            paginated = paginated,
            pageCommand = { "/items $it" },
            itemDecorator = { it },
        )
        val rendered = plainText.serialize(component)

        assertTrue(rendered.contains("--- Items - Page 2/3 ---"))
        assertTrue(rendered.contains("gamma"))
        assertTrue(rendered.contains("delta"))
    }

    @Test
    fun `build single page should omit navigation buttons and optional item events`() {
        var pageCommandCalls = 0
        val component = builder.build(
            title = "One Page",
            items = listOf("only item"),
            pageNumber = 1,
            totalPages = 1,
            pageCommand = {
                pageCommandCalls++
                "/items $it"
            },
            itemDecorator = { it },
        )
        val rendered = plainText.serialize(component)
        val descendants = component.descendants()

        assertTrue(rendered.contains("--- One Page - Page 1/1 ---"))
        assertFalse(rendered.contains("[← Prev]"))
        assertFalse(rendered.contains("[Next →]"))
        assertEquals(0, pageCommandCalls)
        assertTrue(descendants.none { it.clickEvent() != null || it.hoverEvent() != null })
    }

    private fun Component.descendants(): List<Component> {
        return listOf(this) + children().flatMap { it.descendants() }
    }
}
