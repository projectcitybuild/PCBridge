package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.executesSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

@Suppress("UnstableApiUsage")
class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .executesSuspending(plugin) { context ->
                execute(
                    sender = context.source.sender,
                    page = 1,
                )
            }
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin) { context ->
                        execute(
                            sender = context.source.sender,
                            page = context.getArgument("page", Int::class.java),
                        )
                    }
            )
            .build()
    }

    private suspend fun execute(sender: CommandSender, page: Int) {
        val builds = buildRepository.all(page)
        val totalPages = ceil(builds.total.toDouble() / builds.perPage.toDouble()).toInt()
        val miniMessage = MiniMessage.miniMessage()

        sender.sendMessage(
            miniMessage.deserialize(
                "<gray>--- <dark_aqua>Build List</dark_aqua> - Page <white>${builds.currentPage}</white>/<white>$totalPages</white> ---</gray>"
            )
        )

        builds.data.forEach { build ->
            val text = "<gray>#${build.id} \"<aqua>${build.name}</aqua>\" (<white>${build.votes}</white> votes)</gray>"

            sender.sendMessage(
                miniMessage.deserialize(text)
                    // Separate handling here to ensure character escaping in the name
                    .clickEvent(ClickEvent.runCommand("/build ${build.name}"))
                    .hoverEvent(HoverEvent.showText(Component.text("Teleport to ${build.name}")))
            )
        }

        val footer = buildString {
            append("<gray>---</gray>")

            if (page > 1) {
                append(" <click:run_command:'/builds list ${page - 1}'>[← Prev]</click>")
            }
            if (page < totalPages) {
                append(" <click:run_command:'/builds list ${page + 1}'>[Next →]</click>")
            }
            append(" <gray>---</gray>")
        }

        sender.sendMessage(
            miniMessage.deserialize(
                if (totalPages <= 1) "<gray>---</gray>" else footer
            )
        )
    }
}