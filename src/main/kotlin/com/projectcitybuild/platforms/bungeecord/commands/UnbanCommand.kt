package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class UnbanCommand(
    private val proxyServer: ProxyServer,
    private val scheduler: SchedulerProvider,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val logger: LoggerProvider
): BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        return CommandResult.EXECUTED
    }
}