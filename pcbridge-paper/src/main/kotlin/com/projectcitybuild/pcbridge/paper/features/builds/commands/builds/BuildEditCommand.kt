package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.component.join
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildNameSuggester
import com.projectcitybuild.pcbridge.paper.features.builds.data.EditableBuildField
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.Plugin

class BuildEditCommand(
    private val plugin: Plugin,
    private val buildNameSuggester: BuildNameSuggester,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("edit")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, buildNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val build = buildRepository.get(name)
        checkNotNull(build) { "Build not found" }

        val actions = EditableBuildField.entries
        val actionComponents = actions.map { field ->
            val existing = when (field) {
                EditableBuildField.NAME -> build.name
                EditableBuildField.DESCRIPTION -> build.description
                EditableBuildField.LORE -> build.lore
            }
            val command = "/builds set ${build.id} ${field.name} ${existing.orEmpty()}"
            val hoverText = "/builds set ${build.id} $field"

            Component.text()
                .append(Component.text("["))
                .append(
                    Component.text(field.name, NamedTextColor.WHITE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.suggestCommand(command))
                        .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                )
                .append(Component.text("]"))
        }
        val actionComponent = actionComponents.join(
            separator = Component.space()
        )

        player.sendMessage(
            Component.text("Click a field to edit:", NamedTextColor.GRAY)
                .appendNewline()
                .append(actionComponent)
        )
    }
}