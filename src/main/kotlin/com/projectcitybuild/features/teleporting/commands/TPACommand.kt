package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.teleporting.repositories.TeleportRequestRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.PlatformTimer
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TPACommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val scheduler: PlatformScheduler,
    private val timer: PlatformTimer,
    private val config: PlatformConfig,
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
            requesterUUID = input.player.uniqueId,
            targetUUID = targetPlayer.uniqueId,
            timerIdentifier,
            TeleportRequestRepository.TeleportType.TP_TO_PLAYER,
        )

        timer.scheduleOnce(
            identifier = timerIdentifier,
            delay = config.get(PluginConfig.TP_REQUEST_AUTO_EXPIRE_SECONDS).toLong(),
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

        input.player.send().action("Teleport request sent...")

        targetPlayer.sendMessage(
            TextComponent()
                .add("${input.player.name} would like to teleport to you:\n") {
                    it.isBold = true
                }
                .add(
                    TextComponent("[Accept]").apply {
                        color = ChatColor.GREEN
                        isBold = true
                        isUnderlined = true
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Teleport them to your location"))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept")
                    }
                )
                .add(" ")
                .add(
                    TextComponent("[Decline]").apply {
                        color = ChatColor.RED
                        isBold = true
                        isUnderlined = true
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Decline the request and do nothing"))
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
