package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer

class BanCommand(
        private val proxyServer: ProxyServer,
        private val scheduler: SchedulerProvider,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"

    override fun execute(input: BungeecordCommandInput): CommandResult {

        return CommandResult.EXECUTED
    }
}