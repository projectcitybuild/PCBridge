package com.projectcitybuild.modules.buildtools.invisframes.commands

import com.projectcitybuild.support.spigot.SpigotNamespace
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class InvisFrameCommand(
    private val spigotNamespace: SpigotNamespace,
) : SpigotCommand {
    override val label = "invisframe"
    override val permission = "pcbridge.build.invisframe"
    override val usageHelp = "/invisframe [glowing]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size > 1) {
            throw InvalidCommandArgumentsException()
        }

        val option = input.args.firstOrNull()?.lowercase()
        if (input.args.isNotEmpty() && option != "glowing") {
            throw InvalidCommandArgumentsException()
        }

        val wantsGlowEffect = option == "glowing"

        val itemStack = if (wantsGlowEffect) {
            ItemStack(Material.GLOW_ITEM_FRAME)
        } else {
            ItemStack(Material.ITEM_FRAME)
        }
        val itemName = if (wantsGlowEffect) {
            "Invisible Glowing Frame"
        } else {
            "Invisible Item Frame"
        }

        val player = input.player
        player.inventory.addItem(itemStack.apply {
            /**
             * Since we don't have direct access to NMS editing, we track an invisible frame with a tag.
             * This tag is saved into the persistentDataContainer so that it persists with the object.
             *
             * - This command gives the player a tagged ItemStack of item frames
             * - When the player hangs the frame (HangingPlaceEvent), we apply the tag to the placed entity
             * - When the player places an item in the tagged frame (PlayerInteractEntityEvent), we hide the frame
             * - When the player takes an item from the tagged frame (EntityDamageByEntityEvent), we show the frame
             */
            if(itemMeta == null) throw Exception("ItemMeta cannot be null")
            itemMeta = itemMeta?.apply {
                setDisplayName(itemName)
                persistentDataContainer.set(
                    spigotNamespace.invisibleKey,
                    PersistentDataType.BYTE,
                    1,
                )
            }
        })

        if (wantsGlowEffect) {
            player.send().info("You received an invisible item frame")
        } else {
            player.send().info("You received an invisible glowing frame")
        }
    }
}
