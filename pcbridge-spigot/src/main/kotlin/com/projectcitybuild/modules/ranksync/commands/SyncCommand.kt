package com.projectcitybuild.modules.ranksync.commands

import com.projectcitybuild.features.ranksync.usecases.GenerateAccountVerificationURL
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.add
import com.projectcitybuild.support.textcomponent.send
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SyncCommand(
    private val generateAccountVerificationURL: GenerateAccountVerificationURL,
    private val updatePlayerGroups: UpdatePlayerGroups,
) : SpigotCommand {

    override val label = "sync"
    override val permission = "pcbridge.sync.login"
    override val usageHelp = "/sync [finish]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        when {
            input.args.isEmpty() -> generateVerificationURL(input.player)
            input.args.size == 1 && input.args.first() == "finish" -> syncGroups(input.player)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private suspend fun generateVerificationURL(player: Player) {
        val result = generateAccountVerificationURL.generate(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                GenerateAccountVerificationURL.FailureReason.ALREADY_LINKED
                -> syncGroups(player)

                GenerateAccountVerificationURL.FailureReason.EMPTY_RESPONSE
                -> player.send().error("Failed to generate verification URL: No URL received from server")
            }
            is Success -> player.spigot().sendMessage(
                TextComponent()
                    .add("To link your account, please ")
                    .add("click here") {
                        it.isBold = true
                        it.isUnderlined = true
                        it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, result.value.urlString)
                        it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(result.value.urlString))
                    }
                    .add(" and login if required")
            )
        }
    }

    private suspend fun syncGroups(player: Player) {
        val result = updatePlayerGroups.execute(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
                -> player.send().error("Sync failed. Did you finish registering your account?")
            }
            is Success -> {
                player.send().success("Account linked! Your rank will be automatically synchronized with the PCB network")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("finish")
            else -> null
        }
    }
}
