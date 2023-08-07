package com.projectcitybuild.modules.moderation.bans

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.moderation.bans.actions.AuthoriseConnection
import com.projectcitybuild.modules.moderation.bans.actions.BanIP
import com.projectcitybuild.modules.moderation.bans.actions.BanUUID
import com.projectcitybuild.modules.moderation.bans.actions.CheckUUIDBan
import com.projectcitybuild.modules.moderation.bans.actions.UnbanIP
import com.projectcitybuild.modules.moderation.bans.actions.UnbanUUID
import com.projectcitybuild.modules.moderation.bans.commands.BanCommand
import com.projectcitybuild.modules.moderation.bans.commands.BanIPCommand
import com.projectcitybuild.modules.moderation.bans.commands.CheckBanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanCommand
import com.projectcitybuild.modules.moderation.bans.commands.UnbanIPCommand
import com.projectcitybuild.modules.moderation.bans.listeners.CheckBanOnConnectListener
import com.projectcitybuild.support.commandapi.suspendExecutesPlayer
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.StringArgument

class BansModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("ban") {
            withPermission(Permissions.COMMAND_BANS_BAN)
            withShortDescription("Bans a player from connecting to the server")
            withArguments(
                StringArgument("player")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(container.server.onlinePlayers.map { it.name })
                    ),
                GreedyStringArgument("reason"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                BanCommand(
                    BanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.spigotServer,
                    ),
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                    reason = args.get("reason") as String
                )
            }
        }

        command("banip") {
            withPermission(Permissions.COMMAND_BANS_BAN_IP)
            withShortDescription("Bans an IP address from connecting to the server")
            withArguments(
                StringArgument("player")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(container.server.onlinePlayers.map { it.name })
                    )
                ,
                GreedyStringArgument("reason"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                BanIPCommand(
                    container.server,
                    BanIP(
                        container.ipBanRepository,
                        container.spigotServer,
                    ),
                ).execute(
                    commandSender = player,
                    target = args.get("player") as String,
                    reason = args.get("reason") as String
                )
            }
        }

        command("checkban") {
            withPermission(Permissions.COMMAND_BANS_CHECK_BAN)
            withShortDescription("Checks whether a player is currently banned from the server")
            withArguments(
                StringArgument("player"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                CheckBanCommand(
                    CheckUUIDBan(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.dateTimeFormatter,
                    ),
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                )
            }
        }

        command("unban") {
            withPermission(Permissions.COMMAND_BANS_UNBAN)
            withShortDescription("Allows a banned player to connect to the server again")
            withArguments(
                StringArgument("player"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                UnbanCommand(
                    UnbanUUID(
                        container.playerBanRepository,
                        container.playerUUIDRepository,
                        container.server,
                    ),
                ).execute(
                    commandSender = player,
                    targetPlayerName = args.get("player") as String,
                )
            }
        }

        command("unbanip") {
            withPermission(Permissions.COMMAND_BANS_UNBAN_IP)
            withShortDescription("Allows the given IP to connect to the server")
            withArguments(
                StringArgument("ip"),
            )
            suspendExecutesPlayer(container.plugin) { player, args ->
                UnbanIPCommand(
                    UnbanIP(container.ipBanRepository)
                ).execute(
                    commandSender = player,
                    targetIP = args.get("ip") as String,
                )
            }
        }

        listener(
            CheckBanOnConnectListener(
                container.aggregateRepository,
                AuthoriseConnection(),
                container.logger,
                container.dateTimeFormatter,
                container.errorReporter,
                container.localEventBroadcaster,
            ),
        )
    }
}
