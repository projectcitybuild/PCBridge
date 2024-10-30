package com.projectcitybuild.pcbridge.paper.support.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class CommandHelpBuilder(
    private val usage: String?,
) {
    private val commands: MutableList<CommandHelp> = mutableListOf()

    data class CommandHelp(
        val label: String,
        val description: String,
        val permission: String,
    )

    fun subcommand(
        label: String,
        description: String,
        permission: String,
    ): CommandHelpBuilder {
        commands.add(
            CommandHelp(label, description, permission),
        )
        return this
    }

    fun build(hasPermission: (String) -> Boolean): TextComponent {
        return Component.text()
            .also {
                if (!usage.isNullOrEmpty()) {
                    it.append(
                        Component.text(usage)
                            .color(NamedTextColor.GRAY)
                            .decorate(TextDecoration.ITALIC)
                    )
                    it.appendNewline()
                }
                if (commands.isNotEmpty()) {
                    it.appendDivider()
                }
                commands.forEach { command ->
                    if (hasPermission(command.permission)) {
                        it.append(command.asComponent())
                    }
                }
                if (commands.isNotEmpty()) {
                    it.appendDivider()
                }
            }
            .build()
    }
}

private fun CommandHelpBuilder.CommandHelp.asComponent(): TextComponent {
    return Component.text()
        .append(
            Component.text(label)
                .color(NamedTextColor.AQUA),
        )
        .append(
            Component.text(" - $description")
                .color(NamedTextColor.LIGHT_PURPLE),
        )
        .appendNewline()
        .build()
}
