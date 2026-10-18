package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.building.buildingTracer
import com.projectcitybuild.pcbridge.features.building.domain.data.ItemRenamedEvent
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.plugin.Plugin

class ItemNameCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val description: String = "Renames the item in your primary hand"

    override fun literal(): PaperCommandNode {
        return Commands.literal("itemname")
            .requiresPermission(PermissionNode.BUILDING_ITEM_RENAME)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(commandTaskExecutor, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildingTracer) {
            val player = context.source.requirePlayer()

            val itemStack = player.inventory.itemInMainHand
            check(itemStack.type != Material.AIR) { l10n.errorNoItemToRename }

            val rawName = context.getArgument("name", String::class.java)
            val name = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName)
            val itemMeta = itemStack.itemMeta
            itemMeta.displayName(name)
            itemStack.setItemMeta(itemMeta)

            player.sendMessage(l10n.renamedItem(name))

            eventBroadcaster.broadcast(
                ItemRenamedEvent(
                    displayName = name,
                    item = itemStack,
                    player = player,
                ),
            )
        }
}
