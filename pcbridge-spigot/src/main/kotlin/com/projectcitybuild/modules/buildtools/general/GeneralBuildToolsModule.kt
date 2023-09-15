package com.projectcitybuild.modules.buildtools.general

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.buildtools.general.commands.BinCommand
import com.projectcitybuild.modules.buildtools.general.listeners.BinCloseListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class GeneralBuildToolsModule: PluginModule {

    override fun register(module: ModuleDeclaration) = module {
        command("bin") {
            withAliases("trash")
            withPermission(Permissions.COMMAND_BUILD_BIN)
            withShortDescription("Opens a temporary menu to dispose of items and blocks")
            executesPlayer(PlayerCommandExecutor { player, _ ->
                BinCommand(container.spigotServer).execute(player)
            })
        }

        listener(
            BinCloseListener()
        )
    }
}