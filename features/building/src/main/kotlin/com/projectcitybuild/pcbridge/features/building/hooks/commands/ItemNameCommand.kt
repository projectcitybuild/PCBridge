package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.building.BuildingPermissions
import com.projectcitybuild.pcbridge.features.building.buildingTracer
import com.projectcitybuild.pcbridge.features.building.domain.data.ItemRenamedEvent
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.plugin.Plugin

class ItemNameCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("itemname") {
        description = "Renames the item in your primary hand"
        requiresPermission(BuildingPermissions.ITEM_RENAME)
        argument("name", StringArgumentType.greedyString()) {
            executesSuspending(commandTaskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(buildingTracer) {
        val player = context.source.requirePlayer()

        val itemStack = player.inventory.itemInMainHand
        check(itemStack.type != Material.AIR) { l10n.building.errorNoItemToRename }

        val rawName = context.getArgument("name", String::class.java)
        val name = LegacyComponentSerializer.legacyAmpersand().deserialize(rawName)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(name)
        itemStack.setItemMeta(itemMeta)

        player.sendMessage(l10n.building.renamedItem(name))

        eventBroadcaster.broadcast(
            ItemRenamedEvent(
                displayName = name,
                item = itemStack,
                player = player,
            ),
        )
    }
}
