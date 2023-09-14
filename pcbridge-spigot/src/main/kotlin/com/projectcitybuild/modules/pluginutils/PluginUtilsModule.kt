package com.projectcitybuild.modules.pluginutils

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.pluginutils.commands.VersionCommand
import com.projectcitybuild.modules.pluginutils.actions.GetVersion
import com.projectcitybuild.modules.pluginutils.actions.ReloadPlugin
import com.projectcitybuild.modules.pluginutils.commands.ReloadCommand
import com.projectcitybuild.modules.pluginutils.listeners.CachePlayerOnJoinListener
import com.projectcitybuild.modules.pluginutils.listeners.ExceptionListener
import com.projectcitybuild.modules.pluginutils.listeners.UncachePlayerOnQuitListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class PluginUtilsModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("pcbridge") {
            withPermission(Permissions.COMMAND_UTILITIES)
            withSubcommand(
                CommandAPICommand("version")
                    .withShortDescription("Shows the plugin version")
                    .executesPlayer(PlayerCommandExecutor { player, _ ->
                        VersionCommand(GetVersion()).execute(player)
                    })
            )
            withSubcommand(
                CommandAPICommand("reload")
                    .withShortDescription("Flushes caches and reloads the plugin config")
                    .executesPlayer(PlayerCommandExecutor { player, _ ->
                        ReloadCommand(
                            ReloadPlugin(
                                container.chatGroupFormatter,
                                container.playerConfigCache,
                                container.warpRepository,
                                container.config,
                            ),
                        ).execute(player)
                    })
            )
        }

        listener(
            ExceptionListener(container.errorReporter)
        )
        listener(
            CachePlayerOnJoinListener(
                container.localEventBroadcaster,
                container.playerConfigRepository,
                container.logger,
                container.time,
            ),
        )
        listener(
            UncachePlayerOnQuitListener(
                container.playerConfigCache,
                container.chatGroupFormatter,
                container.chatBadgeRepository,
            ),
        )
    }
}