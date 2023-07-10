package com.projectcitybuild.modules.pluginutils

import com.projectcitybuild.modules.pluginutils.commands.PCBridgeCommand
import com.projectcitybuild.features.utilities.usecases.GetVersion
import com.projectcitybuild.features.utilities.usecases.ReloadPlugin
import com.projectcitybuild.modules.pluginutils.listeners.CachePlayerOnJoinListener
import com.projectcitybuild.modules.pluginutils.listeners.ExceptionListener
import com.projectcitybuild.modules.pluginutils.listeners.UncachePlayerOnQuitListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class PluginUtilsModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                PCBridgeCommand(
                    GetVersion(),
                    ReloadPlugin(
                        container.chatGroupFormatter,
                        container.playerConfigCache,
                        container.warpRepository,
                        container.config,
                    ),
                ),
            )
            listener(
                ExceptionListener(container.errorReporter)
            )
            listener(
                CachePlayerOnJoinListener(
                    container.localEventBroadcaster,
                    container.playerConfigRepository,
                    container.logger,
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
}