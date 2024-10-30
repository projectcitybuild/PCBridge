package com.projectcitybuild.pcbridge.paper.features.invisframes.commands

import com.projectcitybuild.pcbridge.paper.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotNamespace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class InvisFrameCommand(
    private val spigotNamespace: SpigotNamespace,
) : SpigotCommand<InvisFrameCommand.Args> {
    override val label = "invisframe"

    override val usage = CommandHelpBuilder(usage = "/invisframe [glowing]")

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        val player = sender as? Player
        checkNotNull(player) {
            "Only players can use this command"
        }
        val itemStack =
            if (args.isGlowingFrame) {
                ItemStack(Material.GLOW_ITEM_FRAME)
            } else {
                ItemStack(Material.ITEM_FRAME)
            }
        val itemName =
            if (args.isGlowingFrame) {
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

        if (args.isGlowingFrame) {
            player.sendMessage(
                Component.text("You received an invisible glowing frame")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC),
            )
        } else {
            player.sendMessage(
                Component.text("You received an invisible item frame")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC),
            )
        }
    }

    data class Args(
        val isGlowingFrame: Boolean,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                val isGlowing =
                    if (args.isEmpty()) {
                        false
                    } else if (args[0].lowercase() == "glowing") {
                        true
                    } else {
                        throw BadCommandUsageException()
                    }
                return Args(isGlowingFrame = isGlowing)
            }
        }
    }
}
