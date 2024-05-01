package com.projectcitybuild.support.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor

class CommandHelpBuilder {
    private val commands: MutableList<CommandHelp> = mutableListOf()

    data class CommandHelp(
        val label: String,
        val description: String,
        val permission: String,
    )

    fun command(
        label: String,
        description: String,
        permission: String,
    ): CommandHelpBuilder {
        commands.add(
            CommandHelp(label, description, permission)
        )
        return this
    }

    fun build(hasPermission: (String) -> Boolean): TextComponent {
        check(commands.isNotEmpty()) {
            "Cannot build without commands added"
        }
        return Component.text()
            .appendDivider()
            .also {
                commands.forEach { command ->
                    if (hasPermission(command.permission)) {
                        it.append(command.asComponent())
                    }
                }
            }
            .appendDivider()
            .build()
    }
}

private fun CommandHelpBuilder.CommandHelp.asComponent(): TextComponent {
    return Component.text()
        .append(
            Component.text(label)
                .color(NamedTextColor.AQUA)
        )
        .append(
            Component.text(" - $description")
                .color(NamedTextColor.LIGHT_PURPLE)
        )
        .appendNewline()
        .build()
}