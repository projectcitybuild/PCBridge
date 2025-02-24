package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.LengthAwarePaginator
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

        sender.sendMessage(
            LengthAwarePaginator().component(
                title = "Your Homes",
                paginatedData = homes,
                pageCommandBuilder = { pageIndex -> "/homes list $pageIndex" },
                itemDecorator = { home ->
                    val text = "<gray>#${home.id} \"<aqua>${home.name}</aqua>\"</gray>"

                    MiniMessage.miniMessage().deserialize(text)
                        // Separate handling here to ensure character escaping in the name
                        .clickEvent(ClickEvent.runCommand("/home ${home.name}"))
                        .hoverEvent(HoverEvent.showText(Component.text("Teleport to ${home.name}")))
                },
            )
        )
    }
}