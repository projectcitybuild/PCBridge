package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.players.GetPlayerUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.text.SimpleDateFormat
import java.util.*

class CheckBanCommand(
        private val proxyServer: ProxyServer,
        private val scheduler: SchedulerProvider,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty()) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()

        input.sender.sendMessage(TextComponent("Searching for active bans for $targetPlayerName...").also {
            it.color = ChatColor.GRAY
        })

        GetPlayerUUIDAction(apiRequestFactory, apiClient).execute(
                proxyServer = proxyServer,
                playerName = targetPlayerName
        ) { result ->
            when (result) {
                is Failure -> {
                    scheduler.sync {
                        input.sender.sendMessage(TextComponent("Error: Failed to retrieve UUID of given player").also {
                            it.color = ChatColor.RED
                        })
                    }
                    return@execute
                }
                is Success -> {
                    CheckBanStatusAction(apiRequestFactory, apiClient).execute(
                        playerId = result.value
                    ) { result ->
                        scheduler.sync {
                            if (result is Failure) {
                                when (result.reason) {
                                    is CheckBanStatusAction.FailReason.API_ERROR -> {
                                        input.sender.sendMessage(TextComponent("Error: ${result.reason.message}").also {
                                            it.color = ChatColor.RED
                                        })
                                    }
                                }
                            }
                            if (result is Success) {
                                val ban = result.value
                                if (ban == null) {
                                    input.sender.sendMessage(TextComponent("$targetPlayerName is not currently banned").also {
                                        it.color = ChatColor.GRAY
                                        it.isBold = true
                                    })
                                } else {
                                    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
                                    val banDate = Date(ban.createdAt * 1000)
                                    val banDateString = dateFormatter.format(banDate)

                                    val expireDateString = ban.expiresAt?.let {
                                        dateFormatter.format(Date(it * 1000))
                                    }

                                    val tc = TextComponent()
                                    tc.addExtra(TextComponent("$targetPlayerName is currently banned\n").also {
                                        it.color = ChatColor.AQUA
                                        it.isBold = true
                                    })
                                    tc.addExtra(TextComponent("---\n").also {
                                        it.color = ChatColor.GRAY
                                    })
                                    tc.addExtra(TextComponent("⇒ Reason: ").also {
                                        it.color = ChatColor.GRAY
                                    })
                                    tc.addExtra(TextComponent(ban.reason + "\n").also {
                                        it.color = ChatColor.WHITE
                                    })
                                    tc.addExtra(TextComponent("⇒ Date: ").also {
                                        it.color = ChatColor.GRAY
                                    })
                                    tc.addExtra(TextComponent("$banDateString\n").also {
                                        it.color = ChatColor.WHITE
                                    })
                                    tc.addExtra(TextComponent("⇒ Expires: ").also {
                                        it.color = ChatColor.GRAY
                                    })
                                    tc.addExtra(TextComponent("${(expireDateString ?: "Never")}\n").also {
                                        it.color = ChatColor.WHITE
                                    })
                                    input.sender.sendMessage(tc)
                                }
                            }
                        }
                    }
                }
            }
        }
        return CommandResult.EXECUTED
    }
}