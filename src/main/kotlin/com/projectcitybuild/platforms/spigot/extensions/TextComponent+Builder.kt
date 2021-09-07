package com.projectcitybuild.platforms.spigot.extensions

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent

fun TextComponent.add(component: BaseComponent, config: ((BaseComponent) -> Unit)? = null): TextComponent {
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

fun TextComponent.add(number: Int, config: ((TextComponent) -> Unit)? = null): TextComponent {
    return add(number.toString(), config)
}