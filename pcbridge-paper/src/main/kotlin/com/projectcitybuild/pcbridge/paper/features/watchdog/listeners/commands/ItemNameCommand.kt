package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.commands

import com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.events.ItemRenamedEvent
import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ItemNameCommand(
    private val eventBroadcaster: SpigotEventBroadcaster,
) : SpigotCommand<ItemNameCommand.Args> {
    override val label = "itemname"

    override val usage = CommandHelpBuilder(usage = "/itemname <name>")

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        val itemStack = sender.inventory.itemInMainHand
        check (itemStack.type != Material.AIR) {
            "No item in hand to rename"
        }
        val name = LegacyComponentSerializer.legacyAmpersand().deserialize(args.name)
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

    data class Args(
        val name: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty() || args[0].isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(name = args.joinToString(separator = " "))
            }
        }
    }
}
