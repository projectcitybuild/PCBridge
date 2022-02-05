package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.teleporting.repositories.TeleportRequestRepository
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.scheduler.PlatformScheduler
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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TPACommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val scheduler: PlatformScheduler,
    private val timer: Timer,
): BungeecordCommand {

    override val label: String = "tpa"
    override val permission = "pcbridge.tpa"
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

        val existingRequest = teleportRequestRepository.get(targetPlayer.uniqueId)
        if (existingRequest != null) {
            input.sender.send().error("${targetPlayer.name} already has a pending teleport request. Please try again shortly")
            return
        }

        val timerIdentifier = UUID.randomUUID().toString()

        teleportRequestRepository.set(
            input.player.uniqueId,
            targetPlayer.uniqueId,
            timerIdentifier,
        )

        timer.scheduleOnce(
            identifier = timerIdentifier,
            delay = 15,
            unit = TimeUnit.SECONDS,
        ) {
            val request = teleportRequestRepository.get(targetPlayer.uniqueId)
            if (request != null && request.requesterUUID == input.player.uniqueId) {
                teleportRequestRepository.delete(targetPlayer.uniqueId)
                scheduler.sync {
                    input.player.send().action("Teleport request expired")
                    targetPlayer.send().action("Teleport request expired")
                }
            }
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
