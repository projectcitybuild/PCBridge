package com.projectcitybuild.pcbridge.papersupport.support.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.collections.withIndex

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

fun MiniMessage.deserialize(input: String, resolvers: Iterable<TagResolver>) = deserialize(
    input,
    TagResolver.resolver(resolvers),
)