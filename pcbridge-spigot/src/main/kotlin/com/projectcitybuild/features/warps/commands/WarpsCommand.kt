package com.projectcitybuild.features.warps.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.data.repositories.WarpRepository
import com.projectcitybuild.pcbridge.core.modules.config.Config
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val config: Config<PluginConfig>,
): SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            // TODO
            return true
        }
        when (args[0]) {
            "list" -> ListCommand(
                argsValidator = ListArgsValidator(),
                warpRepository = warpRepository,
                audiences = audiences,
                itemsPerPage = config.get().warps.itemsPerPage,
            ).onCommand(
                sender = sender,
                args = args.toList().drop(1),
            )
        }
        return true
    }
}

class BadCommandArgument(message: String? = null): Exception(message)

class ListCommand(
    private val argsValidator: ListArgsValidator,
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val itemsPerPage: Int,
) {
   suspend fun onCommand(
       sender: CommandSender,
       args: List<String>,
   ) {
       val validated = argsValidator.validate(args)

       val page = warpRepository.all(
           limit = itemsPerPage,
           page = validated.page,
       )
       if (page.items.isEmpty()) {
           audiences.sender(sender).sendMessage(
               Component.text("No warps available")
                   .color(NamedTextColor.GRAY)
           )
           return
       }

       val message = Component.text()

       page.items.withIndex().forEach { (index, warp) ->
           val command = "/warp ${warp.name}"

           message.append(
               Component.text(warp.name)
                   .color(TextColor.color(0x213AA8))
                   .decorate(TextDecoration.UNDERLINED)
                   .clickEvent(ClickEvent.runCommand(command))
                   .hoverEvent(HoverEvent.showText(Component.text(command)))
           )
           if (index < page.items.size - 1) {
               message.append(
                   Component.text(", ")
               )
           }
       }

       audiences.sender(sender).sendMessage(message)
   }
}

class ListArgsValidator {
    data class Args(
        val page: Int,
    )

    fun validate(args: List<String>): Args {
        if (args.size > 1) {
            throw BadCommandArgument()
        }
        val page = if (args.isEmpty()) 0 else args[0].toIntOrNull()
            ?: throw BadCommandArgument("Page must be a number")

        return Args(page = page)
    }
}