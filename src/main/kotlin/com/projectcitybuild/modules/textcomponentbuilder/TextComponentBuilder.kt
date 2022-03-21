package com.projectcitybuild.platforms.bungeecord.extensions

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent

fun TextComponent.add(component: BaseComponent?, config: ((BaseComponent) -> Unit)? = null): TextComponent {
    if (component == null) return this

    this.addExtra(
        component.also {
            config?.invoke(it)
        }
    )
    return this
}

fun TextComponent.add(message: String, config: ((TextComponent) -> Unit)? = null): TextComponent {
    this.addExtra(
        TextComponent(message).also {
            config?.invoke(it)
        }
    )
    return this
}

fun TextComponent.addIf(condition: Boolean, message: String, config: ((TextComponent) -> Unit)? = null): TextComponent {
    if (!condition) return this
    add(message, config)
    return this
}

fun TextComponent.add(number: Int, config: ((TextComponent) -> Unit)? = null): TextComponent {
    return add(number.toString(), config)
}

fun TextComponent.add(collection: Array<out BaseComponent>): TextComponent {
    collection.forEach { this.addExtra(it) }
    return this
}
