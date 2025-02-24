package com.projectcitybuild.pcbridge.paper.core.libs.pagination

import com.projectcitybuild.pcbridge.http.pcb.models.PaginatedResponse
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.math.ceil
import kotlin.math.max

/**
 * Paginator that is only provided a subset (a single page)
 * of the total collection
 */
class LengthAwarePaginator {
    fun <T> component(
        title: String,
        paginatedData: PaginatedResponse<List<T>>,
        pageCommandBuilder: (page: Int) -> String,
        itemDecorator: (T) -> Component,
    ): Component = component(
        title = title,
        items = paginatedData.data,
        currentPage = paginatedData.currentPage,
        totalPages = ceil(paginatedData.total.toDouble() / paginatedData.perPage.toDouble()).toInt(),
        pageCommandBuilder = pageCommandBuilder,
        itemDecorator = itemDecorator,
    )

    /**
     * Creates a [Component] containing the given [items] in a list.
     *
     * - The header contains [currentPage] and [totalPages].
     * - The footer contains clickable buttons to navigate the pages via commands built by [pageCommandBuilder]
     * - Each item in the list is displayed as the result of [itemDecorator]
     */
    fun <T> component(
        title: String,
        items: List<T>,
        currentPage: Int,
        totalPages: Int,
        pageCommandBuilder: (page: Int) -> String,
        itemDecorator: (T) -> Component,
    ): Component = Component.text().apply {
        val current = max(1, currentPage)
        val total = max(1, totalPages)

        it.append(header(title, current, total))
        it.appendNewline()

        items.forEach { item ->
            it.append(itemDecorator(item))
            it.appendNewline()
        }
        it.append(
            footer(current, total, pageCommandBuilder)
        )
    }.build()

    private fun header(
        title: String,
        currentPage: Int,
        totalPages: Int,
    ) = MiniMessage.miniMessage().deserialize(
        "<gray>--- <dark_aqua>$title</dark_aqua> - Page <white>$currentPage</white>/<white>$totalPages</white> ---</gray>"
    )

    private fun footer(
        currentPage: Int,
        totalPages: Int,
        pageCommandBuilder: (page: Int) -> String,
    ) = buildString {
        val divider = "<gray>---</gray>"

        if (totalPages <= 1) {
            append(divider)
            return@buildString
        }
        append(
            mutableListOf<String>().apply {
                add(divider)
                if (currentPage > 1) {
                    add("<white><click:run_command:'${pageCommandBuilder(currentPage - 1)}'>[← Prev]</click></white>")
                }
                if (currentPage < totalPages) {
                    add("<white><click:run_command:'${pageCommandBuilder(currentPage + 1)}'>[Next →]</click></white>")
                }
                add(divider)
            }.joinToString(" ")
        )
    }.let(MiniMessage.miniMessage()::deserialize)
}