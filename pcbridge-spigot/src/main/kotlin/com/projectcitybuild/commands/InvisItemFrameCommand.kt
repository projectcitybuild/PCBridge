package com.projectcitybuild.commands

import com.projectcitybuild.support.spigot.SpigotNamespace
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class InvisItemFrameCommand(
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
