package com.projectcitybuild.features.mail.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.mail.usecases.GetAllMailUseCase
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
    private val getAllMailUseCase: GetAllMailUseCase,
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
            "clear" -> markAllAsRead()
            "send" -> sendMail()
        }
    }

    private fun showMessages(player: ProxiedPlayer, input: BungeecordCommandInput) {
        val page = when (input.args.size) {
            1 -> 1
            2 -> runCatching { input.args[1].toInt() }.getOrNull() ?: 1
            else -> throw InvalidCommandArgumentsException()
        }

        val result = getAllMailUseCase.getMail(player.uniqueId, page)

        when (result) {
            is Failure -> when(result.reason) {
                GetAllMailUseCase.FailureReason.PAGE_TOO_HIGH
                    -> player.send().info("You do not have that many pages of mail to read")

                GetAllMailUseCase.FailureReason.NO_MAIL
                    -> player.send().info("You do not have any unread mail")
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

    private fun markAllAsRead() {

    }

    private fun sendMail() {

    }
}