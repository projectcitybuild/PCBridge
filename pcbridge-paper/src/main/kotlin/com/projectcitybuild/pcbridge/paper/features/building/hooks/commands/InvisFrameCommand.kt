package com.projectcitybuild.pcbridge.paper.features.building.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSync
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.features.building.buildingTracer
import com.projectcitybuild.pcbridge.paper.features.building.domain.data.InvisFrameKey
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class InvisFrameCommand(
    private val plugin: Plugin,
    private val spigotNamespace: SpigotNamespace,
) : BrigadierCommand {
    override val description: String = "Gives you an itemframe that turns invisible when holding an item"

    override fun literal(): PaperCommandNode {
        return Commands.literal("invisframe")
            .requiresPermission(PermissionNode.BUILDING_INVIS_FRAME)
            .then(
                Commands.literal("glowing")
                    .executesSuspending(plugin, ::giveGlowing)
            )
            .executesSuspending(plugin, ::giveNormal)
            .build()
    }

    private fun giveNormal(context: PaperCommandContext) = give(
        context = context,
        glowing = false,
    )

    private fun giveGlowing(context: PaperCommandContext) = give(
        context = context,
        glowing = true,
    )

    private fun give(
        context: PaperCommandContext,
        glowing: Boolean,
    ) = context.scopedSync(buildingTracer) {
        val player = context.source.requirePlayer()
        val itemStack = if (glowing) {
            ItemStack(Material.GLOW_ITEM_FRAME)
        } else {
            ItemStack(Material.ITEM_FRAME)
        }
        val itemName = if (glowing) {
            "Invisible Glowing Frame"
        } else {
            "Invisible Item Frame"
        }
        player.inventory.addItem(
            itemStack.apply {
                editMeta { meta ->
                    meta.displayName(
                        Component.text(itemName)
                            .color(NamedTextColor.LIGHT_PURPLE)
                            .decorate(TextDecoration.ITALIC),
                    )
                }
                editPersistentDataContainer { pdc ->
                    pdc.set(
                        spigotNamespace.get(InvisFrameKey),
                        PersistentDataType.BOOLEAN,
                        true,
                    )
                }
            },
        )
        player.sendRichMessage(
            if (glowing) l10n.receivedInvisFrameGlowing
            else l10n.receivedInvisFrame
        )
    }
}
