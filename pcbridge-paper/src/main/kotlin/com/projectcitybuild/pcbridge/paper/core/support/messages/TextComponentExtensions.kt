package com.projectcitybuild.pcbridge.paper.core.support.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

@Deprecated("Unnecessary extension")
fun TextComponent.Builder.appendDivider(color: TextColor = NamedTextColor.LIGHT_PURPLE): TextComponent.Builder {
    return append(
        Component.text("-----------")
            .color(color)
            .appendNewline(),
    )
}
