package com.projectcitybuild.pcbridge.paper.features.building.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.catchSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.building.events.ItemRenamedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.plugin.Plugin

class ItemNameCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override val description: String = "Renames the item in your primary hand"

    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("itemname")
            .requiresPermission(PermissionNode.BUILDING_ITEM_RENAME)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.catchSuspending {
        val player = context.source.requirePlayer()

        val itemStack = player.inventory.itemInMainHand
        check(itemStack.type != Material.AIR) { l10n.errorNoItemToRename }

        val rawName = context.getArgument("name", String::class.java)
        val name = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(name)
        itemStack.setItemMeta(itemMeta)

        player.sendRichMessage(
            l10n.renamedItem("<name>"),
            Placeholder.component("name", name),
        )
        eventBroadcaster.broadcast(
            ItemRenamedEvent(
                displayName = name,
                item = itemStack,
                player = player,
            )
        )
    }
}
