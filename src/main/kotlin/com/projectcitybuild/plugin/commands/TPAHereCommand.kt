package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.timer.PlatformTimer
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.TeleportRequestRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Server
import org.bukkit.command.CommandSender
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TPAHereCommand @Inject constructor(
    private val server: Server,
    private val nameGuesser: NameGuesser,
    private val teleportRequestRepository: TeleportRequestRepository,
    private val scheduler: PlatformScheduler,
    private val timer: PlatformTimer,
    private val config: PlatformConfig,
) : SpigotCommand {

    override val label: String = "tpahere"
    override val permission = "pcbridge.tpahere"
    override val usageHelp = "/tpahere <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }
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
            TeleportRequestRepository.TeleportType.SUMMON_PLAYER,
        )

        timer.scheduleOnce(
            identifier = timerIdentifier,
            delay = config.get(ConfigKey.TP_REQUEST_AUTO_EXPIRE_SECONDS).toLong(),
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

        input.player.send().action("Summon request sent...")

        targetPlayer.spigot().sendMessage(
            TextComponent()
                .add("${input.player.name} would like to summon you to their location:\n") {
                    it.isBold = true
                }
                .add(
                    TextComponent("[Accept]").apply {
                        color = ChatColor.GREEN
                        isBold = true
                        isUnderlined = true
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Teleport to their location"))
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
            args.isEmpty() ->
                server.onlinePlayers
                    .map { it.name }
                    .filter { it != sender?.name }

            args.size == 1 ->
                server.onlinePlayers
                    .map { it.name }
                    .filter { it != sender?.name }
                    .filter { it.lowercase().startsWith(args.first().lowercase()) }

            else -> null
        }
    }
}
