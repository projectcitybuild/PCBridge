package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.math.ceil
import kotlin.math.max

class HomeListCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.HOMES_USE)
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val page = context.getOptionalArgument("page", Int::class.java) ?: 1
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val homes = homeRepository.all(playerUUID = sender.uniqueId, page = page)
        val totalPages = max(1, ceil(homes.total.toDouble() / homes.perPage.toDouble()).toInt())
        val miniMessage = MiniMessage.miniMessage()

        // TODO: combine this with the logic in BuildListCommand
        sender.sendMessage(
            miniMessage.deserialize(
                "<gray>--- <dark_aqua>Your Homes</dark_aqua> - Page <white>${homes.currentPage}</white>/<white>$totalPages</white> ---</gray>"
            )
        )

        homes.data.forEach { home ->
            val text = "<gray>#${home.id} \"<aqua>${home.name}</aqua>\"</gray>"

            sender.sendMessage(
                miniMessage.deserialize(text)
                    // Separate handling here to ensure character escaping in the name
                    .clickEvent(ClickEvent.runCommand("/home ${home.name}"))
                    .hoverEvent(HoverEvent.showText(Component.text("Teleport to ${home.name}")))
            )
        }

        val footer = buildString {
            append("<gray>---</gray>")

            if (page > 1) {
                append(" <click:run_command:'/homes list ${page - 1}'>[← Prev]</click>")
            }
            if (page < totalPages) {
                append(" <click:run_command:'/homes list ${page + 1}'>[Next →]</click>")
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