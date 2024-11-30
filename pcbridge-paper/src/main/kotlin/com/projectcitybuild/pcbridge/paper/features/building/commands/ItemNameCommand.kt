package com.projectcitybuild.pcbridge.paper.features.building.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import com.projectcitybuild.pcbridge.paper.features.building.events.ItemRenamedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ItemNameCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override val description: String = "Renames the item in your primary hand"

    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("itemname")
            .requiresPermission(PermissionNode.BUILDING_ITEM_RENAME)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val itemStack = sender.inventory.itemInMainHand
        check(itemStack.type != Material.AIR) { "No item in hand to rename" }

        val rawName = context.getArgument("name", String::class.java)
        val name = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(name)
        itemStack.setItemMeta(itemMeta)

        sender.sendMessage(
            MiniMessage.miniMessage().deserialize(
                "<gray>Renamed item in hand to <red><name></red></gray>",
                Placeholder.component("name", name),
            )
        )
        eventBroadcaster.broadcast(
            ItemRenamedEvent(
                displayName = name,
                item = itemStack,
                player = sender,
            )
        )
    }
}
