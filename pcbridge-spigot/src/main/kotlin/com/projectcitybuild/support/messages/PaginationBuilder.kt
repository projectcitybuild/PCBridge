package com.projectcitybuild.support.messages

import com.projectcitybuild.core.pagination.Page
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor

typealias PageItemTransformer<T> = (IndexedValue<T>) -> TextComponent

class PaginationBuilder<T> {
    private var transformer: PageItemTransformer<T>? = null

    fun items(transformer: PageItemTransformer<T>): PaginationBuilder<T> {
        this.transformer = transformer
        return this
    }

    fun build(page: Page<T>): TextComponent {
        checkNotNull(transformer)

        val message = Component.text()
            .append(
                Component.text("---")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )
            .append(
                Component.text("Page ${page.page} of ${page.totalPages}")
            )
            .append(
                Component.text("---")
                    .color(NamedTextColor.LIGHT_PURPLE)
            )
            .appendNewline()

        for (entry in page.items.withIndex()) {
            val component = transformer!!(entry)
            message.append(component)
        }

        return message.build()
    }
}