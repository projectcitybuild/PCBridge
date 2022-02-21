package com.projectcitybuild.features.mail.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.mail.usecases.ClearMailUseCase
import com.projectcitybuild.features.mail.usecases.GetUnclearedMailUseCase
import com.projectcitybuild.features.mail.usecases.SendMailUseCase
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import javax.inject.Inject

class MailCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
    private val getUnclearedMailUseCase: GetUnclearedMailUseCase,
    private val clearMailUseCase: ClearMailUseCase,
    private val sendMailUseCase: SendMailUseCase,
): BungeecordCommand {

    override val label: String = "mail"
    override val permission = "pcbridge.mail"
    override val usageHelp = "/mail"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        when (input.args.firstOrNull()) {
            null -> throw InvalidCommandArgumentsException()
            "read" -> showMessages(input.player, input)
            "clear" -> clearMail(input.player, input)
            "send" -> sendMail(input.player, input)
        }
    }

    private fun showMessages(player: ProxiedPlayer, input: BungeecordCommandInput) {
        val page = when (input.args.size) {
            1 -> 1
            2 -> runCatching { input.args[1].toInt() }.getOrNull() ?: 1
            else -> throw InvalidCommandArgumentsException()
        }

        val result = getUnclearedMailUseCase.getMail(player.uniqueId, page)

        when (result) {
            is Failure -> when(result.reason) {
                GetUnclearedMailUseCase.FailureReason.PAGE_TOO_HIGH
                    -> player.send().info("You do not have that many pages of mail to read")

                GetUnclearedMailUseCase.FailureReason.NO_MAIL
                    -> player.send().info("You do not have any unread mail")

                GetUnclearedMailUseCase.FailureReason.INVALID_PAGE_NUMBER
                    -> player.send().error("Page must be greater than 0")

            }
            is Success -> {
                val unreadMail = result.value

                player.sendMessage(
                    TextComponent()
                        .add("Mail (Page $page/${unreadMail.totalCount})\n")
                        .add("${unreadMail.formattedSendDate}\n")
                        .add("${unreadMail.mail.senderName}: ${unreadMail.mail.message}")
                )
            }
        }
    }

    private fun clearMail(player: ProxiedPlayer, input: BungeecordCommandInput) {
        val page = when(input.args.size) {
            1 -> null
            2 -> runCatching { input.args[1].toInt() }.getOrNull() ?: throw InvalidCommandArgumentsException()
            else -> throw InvalidCommandArgumentsException()
        }

        val result = clearMailUseCase.clearMail(player.uniqueId, page)

        when (result) {
            is Failure -> when (result.reason) {
                ClearMailUseCase.FailureReason.PAGE_TOO_HIGH
                    -> player.send().info("You do not have that many pages of mail to read")

                ClearMailUseCase.FailureReason.INVALID_PAGE_NUMBER
                -> player.send().error("Page must be greater than 0")
            }
            is Success -> player.send().success("Mail cleared")
        }
    }

    private fun sendMail(player: ProxiedPlayer, input: BungeecordCommandInput) {
        if (input.args.size < 3) {
            throw InvalidCommandArgumentsException()
        }
        val message = input.args.joinWithWhitespaces(2 until input.args.size)
            ?: throw InvalidCommandArgumentsException()

        val targetPlayerName = input.args[1]
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            player.send().error("Player not found")
            return
        }
        sendMailUseCase.sendMail(
            player.uniqueId,
            player.name,
            targetPlayer.uniqueId,
            targetPlayer.name,
            message,
        )

        player.send().success("Mail sent to ${targetPlayer.name}")
    }
}