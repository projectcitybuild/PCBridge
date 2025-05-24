package com.projectcitybuild.pcbridge.paper.core.libs.pagination

import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.math.ceil

class PageComponentBuilder {
    fun <T> build(
        title: String,
        paginated: PaginatedList<T>,
        pageCommand: (Int) -> String,
        itemClickCommand: ((T) -> String)? = null,
        itemHover: ((T) -> String)? = null,
        itemDecorator: (T) -> String,
    ): Component = build(
        title = title,
        items = paginated.data,
        pageNumber = paginated.currentPage,
        totalPages = ceil(paginated.total.toDouble() / paginated.perPage.toDouble()).toInt(),
        pageCommand = pageCommand,
        itemClickCommand = itemClickCommand,
        itemHover = itemHover,
        itemDecorator = itemDecorator,
    )

    fun <T> build(
        title: String,
        items: List<T>,
        pageNumber: Int,
        totalPages: Int,
        pageCommand: (Int) -> String,
        itemClickCommand: ((T) -> String)? = null,
        itemHover: ((T) -> String)? = null,
        itemDecorator: (T) -> String,
    ): Component {
        val miniMessage = MiniMessage.miniMessage()
        var component: Component = Component.empty()

        component = component.append(
            header(title, pageNumber, totalPages)
        )
        items.forEach {
            var decorated = miniMessage.deserialize(
                itemDecorator(it)
            )
            if (itemClickCommand != null) {
                decorated = decorated
                    // Not using MiniMessage for click to ensure character escaping for the name
                    .clickEvent(ClickEvent.runCommand(itemClickCommand(it)))
            }
            if (itemHover != null) {
                decorated = decorated
                    // Not using MiniMessage for click to ensure character escaping for the name
                    .hoverEvent(HoverEvent.showText(Component.text(itemHover(it))))
            }
            component = component.append(decorated)
            component = component.appendNewline()
        }
        component = component.append(
            footer(pageNumber, totalPages, pageCommand)
        )
        return component
    }

    private fun header(
        title: String,
        pageNumber: Int,
        totalPages: Int,
    ): Component {
        val miniMessage = MiniMessage.miniMessage()

        return miniMessage.deserialize(
            "<gray>--- <dark_aqua>$title</dark_aqua> - Page <white>$pageNumber</white>/<white>$totalPages</white> ---</gray><newline>",
        )
    }

    private fun footer(
        pageNumber: Int,
        totalPages: Int,
        pageCommand: (Int) -> String,
    ): Component {
        val miniMessage = MiniMessage.miniMessage()

        if (totalPages <= 1) {
            return miniMessage.deserialize("<gray>---</gray>")
        }

        var component = miniMessage.deserialize("<gray>---</gray> ")
        if (pageNumber > 1) {
            component = component.append(
                prevButton(command = pageCommand(pageNumber - 1)),
            )
        }
        if (pageNumber < totalPages) {
            component = component.append(
                nextButton(command = pageCommand(pageNumber + 1)),
            )
        }
        component = component.append(
            miniMessage.deserialize("<gray>---</gray>")
        )
        return component
    }

    private fun prevButton(command: String): Component {
        return MiniMessage.miniMessage().deserialize("${l10n.pagePrevButton} ")
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(l10n.pagePrevButtonHover)))
    }

    private fun nextButton(command: String): Component {
        return MiniMessage.miniMessage().deserialize("${l10n.pageNextButton} ")
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(l10n.pageNextButtonHover)))
    }
}