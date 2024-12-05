package com.projectcitybuild.pcbridge.paper.core.support.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Joins a collection of components together with the given [separator]
 * inserted in between each collection element
 */
fun List<TextComponent.Builder>.join(separator: Component): TextComponent.Builder {
    val component = Component.text()
    withIndex().forEach { entry ->
        component.append(entry.value)

        val isLast = entry.index == size - 1
        if (!isLast) {
            component.append(separator)
        }
    }
    return component
}