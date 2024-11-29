package com.projectcitybuild.pcbridge.paper.features.invisframes.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class InvisFrameCommand(
    private val plugin: Plugin,
    private val spigotNamespace: SpigotNamespace,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("invisframe")
            .requiresPermission(PermissionNode.BUILD_INVIS_FRAME)
            .then(
                Commands.literal("glowing")
                    .executesSuspending(plugin, ::giveGlowing)
            )
            .executesSuspending(plugin, ::giveNormal)
            .build()
    }

    // TODO: doesn't need to be suspending
    private suspend fun giveNormal(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        give(executor = context.source.executor, glowing = false)
    }

    // TODO: doesn't need to be suspending
    private suspend fun giveGlowing(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        give(executor = context.source.executor, glowing = true)
    }

    private fun give(executor: Entity?, glowing: Boolean) {
        val player = executor as? Player
        checkNotNull(player) {
            "Only players can use this command"
        }
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
                /**
                 * Since we don't have direct access to NMS editing, we track whether a frame
                 * should be invisible using a tag. This tag is saved into the world's
                 * "persistentDataContainer" so that it persists with the actual block data.
                 *
                 * - When the player hangs the frame (HangingPlaceEvent), we apply the tag to the placed entity
                 * - When the player places an item in the tagged frame (PlayerInteractEntityEvent), we hide the frame
                 * - When the player takes an item from the tagged frame (EntityDamageByEntityEvent), we show the frame
                 */
                /**
                 * Since we don't have direct access to NMS editing, we track whether a frame
                 * should be invisible using a tag. This tag is saved into the world's
                 * "persistentDataContainer" so that it persists with the actual block data.
                 *
                 * - When the player hangs the frame (HangingPlaceEvent), we apply the tag to the placed entity
                 * - When the player places an item in the tagged frame (PlayerInteractEntityEvent), we hide the frame
                 * - When the player takes an item from the tagged frame (EntityDamageByEntityEvent), we show the frame
                 */
                if (itemMeta == null) throw Exception("ItemMeta cannot be null")
                itemMeta =
                    itemMeta?.apply {
                        displayName(
                            Component.text(itemName)
                                .color(NamedTextColor.LIGHT_PURPLE)
                                .decorate(TextDecoration.ITALIC),
                        )
                        persistentDataContainer.set(
                            spigotNamespace.invisibleKey,
                            PersistentDataType.BYTE,
                            1,
                        )
                    }
            },
        )

        val message = if (glowing) "You received an invisible glowing frame"
            else "You received an invisible item frame"

        player.sendMessage(
            Component.text(message)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC),
        )
    }
}
