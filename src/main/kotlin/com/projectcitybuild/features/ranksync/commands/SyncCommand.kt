package com.projectcitybuild.features.ranksync.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.ranksync.usecases.GenerateAccountVerificationURLUseCase
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.connection.ProxiedPlayer
import javax.inject.Inject

class SyncCommand @Inject constructor(
    private val generateAccountVerificationURLUseCase: GenerateAccountVerificationURLUseCase,
    private val updatePlayerGroupsUseCase: UpdatePlayerGroupsUseCase,
) : BungeecordCommand {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"
    override val usageHelp = "/sync [finish]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        when {
            input.args.isEmpty() -> generateVerificationURL(input.player)
            input.args.size == 1 && input.args.first() == "finish" -> syncGroups(input.player)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private suspend fun generateVerificationURL(player: ProxiedPlayer) {
        val result = generateAccountVerificationURLUseCase.generate(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                GenerateAccountVerificationURLUseCase.FailureReason.ALREADY_LINKED
                -> syncGroups(player)

                GenerateAccountVerificationURLUseCase.FailureReason.EMPTY_RESPONSE
                -> player.send().error("Failed to generate verification URL: No URL received from server")
            }
            is Success -> player.sendMessage(
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

    private suspend fun syncGroups(player: ProxiedPlayer) {
        val result = updatePlayerGroupsUseCase.sync(player.uniqueId)

        when (result) {
            is Failure -> when (result.reason) {
                UpdatePlayerGroupsUseCase.FailureReason.ACCOUNT_NOT_LINKED
                -> player.send().error("Sync failed. Did you finish registering your account?")

                UpdatePlayerGroupsUseCase.FailureReason.PERMISSION_USER_NOT_FOUND
                -> player.send().error("Permission user not found. Check that the user exists in the Permission plugin")
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
