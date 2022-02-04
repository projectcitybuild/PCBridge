package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.Timer
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import javax.inject.Inject

class TPACommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
    private val timer: Timer,
): BungeecordCommand {

    override val label: String = "tpa"
    override val permission = "pcbridge.tpa.use"
    override val usageHelp = "/tpa <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot teleport to yourself")
            return
        }

        targetPlayer.sendMessage(
            TextComponent()
                .add("${input.player.name} would like to teleport to you:\n")
                .add(
                    TextComponent("[Accept]").apply {
                        color = ChatColor.GREEN
                        isBold = true
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/tpaccept"))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept")
                    }
                )
                .add(" ")
                .add(
                    TextComponent("[Decline]").apply {
                        color = ChatColor.RED
                        isBold = true
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("/tpdeny"))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny")
                    }
                )
                .add("\n")
                .add("(Request will expire in 15 seconds)") {
                    it.color = ChatColor.GRAY
                    it.isItalic = true
                }
        )
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players
                .map { it.name }
                .filter { it != sender?.name }

            args.size == 1 -> proxyServer.players
                .map { it.name }
                .filter { it != sender?.name }
                .filter { it.lowercase().startsWith(args.first().lowercase()) }

            else -> null
        }
    }
}
