package com.projectcitybuild.features.mail.commands

import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class MailCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
): BungeecordCommand {

    override val label: String = "mail"
    override val permission = "pcbridge.mail"
    override val usageHelp = "/mail"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {

        } else {

        }
    }

    private fun showMessages() {

    }

    private fun markAllAsRead() {

    }
}