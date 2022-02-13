package com.projectcitybuild.features.mail.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class MailSendCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val nameGuesser: NameGuesser,
): BungeecordCommand {

    override val label: String = "mailsend"
    override val permission = "pcbridge.mail"
    override val usageHelp = "/mailsend"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.player.send().error("Could not find $targetPlayerName online")
            return
        }
        if (input.player == targetPlayer) {
            input.player.send().error("You cannot mail yourself yourself")
            return
        }
    }
}