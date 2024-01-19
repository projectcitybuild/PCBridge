package com.projectcitybuild.modules.buildtools.bin

import com.projectcitybuild.entities.Permissions
import com.projectcitybuild.modules.buildtools.bin.commands.BinCommand
import com.projectcitybuild.modules.buildtools.bin.listeners.BinCloseListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class BinModule: PluginModule {

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